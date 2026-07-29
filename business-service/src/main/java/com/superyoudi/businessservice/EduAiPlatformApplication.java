package com.superyoudi.businessservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@SpringBootApplication(scanBasePackages = {"com.superyoudi"})
@EnableNeo4jRepositories(basePackages = {"com.superyoudi.kdbservice.repository"})
public class EduAiPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(EduAiPlatformApplication.class, args);
    }

}
