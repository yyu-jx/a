package com.superyoudi.businessservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.superyoudi"})
public class EduAiPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(EduAiPlatformApplication.class, args);
    }

}
