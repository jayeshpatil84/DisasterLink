package com.disasterlink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DisasterLinkApplication {

    public static void main(String[] args) {
        SpringApplication.run(DisasterLinkApplication.class, args);
    }
}
