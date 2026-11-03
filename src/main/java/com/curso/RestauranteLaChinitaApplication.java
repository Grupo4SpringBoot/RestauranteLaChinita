package com.curso;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@SpringBootApplication
public class RestauranteLaChinitaApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestauranteLaChinitaApplication.class, args);
    }

}
