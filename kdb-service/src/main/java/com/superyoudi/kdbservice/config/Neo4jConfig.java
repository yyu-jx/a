package com.superyoudi.kdbservice.config;

import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Driver;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.neo4j.Neo4jVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Sijayyx
 */
@Slf4j
@Configuration
public class Neo4jConfig {
//    @Bean
//    public VectorStore vectorStore(Driver driver, EmbeddingModel embeddingModel) {
//
//        log.info("正在初始化VectorStore。");
//
//        return Neo4jVectorStore.builder(driver, embeddingModel)
//                .distanceType(Neo4jVectorStore.Neo4jDistanceType.COSINE)
//                .indexName("superyoudi-index")
//                .initializeSchema(true)
//                .build();
//    }

}
