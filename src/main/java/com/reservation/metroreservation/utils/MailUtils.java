package com.reservation.metroreservation.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.extra.mail.MailUtil;

public class MailUtils {

    public static void sendResMail(String email, String title, String content){
        MailUtil.send(email, "【地铁自动预约系统】" + DateUtil.tomorrow().toString("yyyyMMdd") + "日地铁 - " + title, content + "- 邮件来自-北京地铁预约出行-自动抢票程序", false);
    }

    public static void sendMail(String email, String title){
        MailUtil.send(email, "【地铁自动预约系统】" + title,"- 邮件来自-北京地铁预约出行-自动抢票程序", false);
    }

}