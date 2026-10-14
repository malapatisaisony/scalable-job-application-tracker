package com.saisony.jobtracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
@SpringBootApplication
@EnableCaching
public class ScalableJobApplicationTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScalableJobApplicationTrackerApplication.class, args);
    }

}
