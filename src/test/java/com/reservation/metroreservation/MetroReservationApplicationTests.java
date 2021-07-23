package com.reservation.metroreservation;

import cn.hutool.http.HttpUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

//@SpringBootTest
public class MetroReservationApplicationTests {

    @Test
    public void contextLoads() {
        System.out.println(HttpUtil.get("https://tool.bitefu.net/jiari/?d=20210919"));
    }

}
