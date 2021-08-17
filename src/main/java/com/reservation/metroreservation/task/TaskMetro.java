package com.reservation.metroreservation.task;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.reservation.metroreservation.utils.MailUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TaskMetro {

    private String authorization;

    private String time;

    private String email;

    private Boolean isReservation = true;

    public TaskMetro(@Value("${metro.authorization}") String authorization, @Value("${metro.time}") String time, @Value("${metro.email}") String email){
        this.authorization = authorization;
        this.time = time;
        this.email = email;
        System.out.println("地铁预约出行抢票系统已开启！");
        MailUtils.sendMail(email, "已开启！");
        checkTomorrowIsHoliday();
        checkToken();
    }

    /**
     * 检查今天是否需要抢票
     */
    @Scheduled(cron = "00 00 09 * * ?")
    public void checkTomorrowIsHoliday(){
        String res = HttpUtil.get("https://tool.bitefu.net/jiari/?d=" + DateUtil.tomorrow().toString("yyyyMMdd"));
        System.out.println("检查一下明天是不是假期: " + res);
        if ("0".equals(res)){
            System.out.println("嘤嘤嘤明天要上班，还是需要抢票滴！！");
            isReservation = true;
        }else{
            System.out.println("明个放假，不用抢票啦！！");
            isReservation = false;
        }
    }

    /**
     * 检查token是否过期
     */
    @Scheduled(cron = "00 00 14 * * ?")
    public void checkToken(){
        String aToken = Base64.decodeStr(authorization);
        String[] aTokens = aToken.split(",");
        DateTime tokenTime = new DateTime(Long.parseLong(aTokens[1]));
        LocalDateTime tokenRxpireTime = LocalDateTimeUtil.of(tokenTime);
        DateTime dateTime = new DateTime(DateUtil.tomorrow().toString("yyyy-MM-dd") + " 23:59:59", DatePattern.NORM_DATETIME_FORMAT);
        LocalDateTime reservationTime = LocalDateTimeUtil.of(dateTime);
        if (tokenRxpireTime.isBefore(reservationTime)){
            System.out.println("您的token将在一天后过期，请尽快修改！");
            MailUtils.sendMail(email, "您的token将在一天后过期，请尽快修改！");
        }else {
            System.out.println("token检查完成，未过期！");
        }
        String time = HttpUtil.get("https://webapi.mybti.cn/Home/GetSystemTime");
        System.out.println("服务器时间比较：");
        System.out.println("目标：" + time);
        System.out.println("本机：" + LocalDateTime.now());
    }


    @Scheduled(cron = "00 00 12 * * ?")
    public void startReservation(){
        if (!isReservation)
            return;

        Boolean flag = false;
        int count = 0;

        JSONObject param = new JSONObject();
        param.set("lineName", "昌平线");
        param.set("snapshotWeekOffset", 0);
        param.set("stationName", "沙河站");
        param.set("enterDate", DateUtil.tomorrow().toString("yyyyMMdd"));
        param.set("snapshotTimeSlot", "0630-0930");
        param.set("timeSlot", time);

        System.out.println("地铁预约参数组装完成"+param.toString());

        while (count < 3 && !flag){
            System.out.println(LocalDateTime.now() + ": 第"+(count+1)+"次请求预约接口");
            String resultStr = HttpRequest.post("https://webapi.mybti.cn/Appointment/CreateAppointment")
                    .header(Header.AUTHORIZATION, authorization)//头信息，多个头信息多次调用此方法即可
                    .header(Header.CONTENT_TYPE, "application/json;charset=UTF-8")
                    .header("user-agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1")
                    .body(param.toString())
                    .timeout(10000)//超时，毫秒
                    .execute().body();
            System.out.println(LocalDateTime.now() + ": 第"+(count+1)+"次预约结果返回值为："+resultStr);
            if (resultStr != null){
                JSONObject res = JSONUtil.parseObj(resultStr);
                if (null != res.get("balance")){
                    if ((Integer)res.get("balance") > 0){
                        System.out.println(LocalDateTime.now() + ": 恭喜您第"+(count+1)+"次预约成功，明天不用排队啦！");
                        flag = true;
                    }
                }else{
                    System.out.println(LocalDateTime.now() + ": 第"+(count+1)+"次预约失败");
                }
            }else{
                System.out.println(LocalDateTime.now() + ": 第"+(count+1)+"次预约失败");
            }
            count++;
        }

        if (flag){
            MailUtils.sendResMail(email, "预约成功！","");
        }else{
            MailUtils.sendResMail(email, "预约失败！","");
        }

        System.out.println(LocalDateTime.now() + ": 定时任务执行完成");
    }
}
