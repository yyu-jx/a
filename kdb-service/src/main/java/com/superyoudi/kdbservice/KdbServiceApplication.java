package com.superyoudi.kdbservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

/**
 * @author Sijayyx
 */
@SpringBootApplication(scanBasePackages = {"com.superyoudi"})
@EnableNeo4jRepositories(basePackages = {"com.superyoudi.kdbservice.repository"})
class KdbServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(KdbServiceApplication.class, args);
    }

}
