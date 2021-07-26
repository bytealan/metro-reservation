package com.reservation.metroreservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MetroReservationApplication {

    public static void main(String[] args) {
        SpringApplication.run(MetroReservationApplication.class, args);
    }

}
