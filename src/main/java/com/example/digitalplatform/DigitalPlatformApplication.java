package com.example.digitalplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DigitalPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(DigitalPlatformApplication.class, args);
    }

}
