package com.reservation.metroreservation.task;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.Date;


@Configuration
@EnableScheduling
public class TaskMetro {

    @Value("${metro.authorization}")
    private String authorization;

    @Value("${metro.time}")
    private String time;

    private Boolean isReservation = true;

    public TaskMetro(){
        System.out.println("地铁预约出行抢票系统已开启！");
        checkTomorrowIsHoliday();
    }

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

        while (!flag && count < 5){
            System.out.println(LocalDateTime.now() + ": 请求预约接口，第"+(count+1)+"次");
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
                if ((Integer)res.get("balance") > 0){
                    System.out.println(LocalDateTime.now() + ": 恭喜您第"+(count+1)+"次预约成功，明天不用排队啦！");
                }else{
                    System.out.println(LocalDateTime.now() + ": 唉，又要排队了，被预约光了！");
                }
                flag = true;
            }else{
                System.out.println(LocalDateTime.now() + ": 第"+(count+1)+"次预约失败");
            }
        }

        System.out.println(LocalDateTime.now() + ": 定时任务执行完成");
    }
}
