package com.superyoudi.businessservice;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@Slf4j
@SpringBootApplication(scanBasePackages = "com.superyoudi.*")
@MapperScan(basePackages = "com.superyoudi.**.mapper")
@EnableNeo4jRepositories(basePackages = {"com.superyoudi.kdbservice.repository"})
public class EduAiPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(EduAiPlatformApplication.class, args);
        log.info("EduAiPlatformApplication 已启动。");
    }

}
