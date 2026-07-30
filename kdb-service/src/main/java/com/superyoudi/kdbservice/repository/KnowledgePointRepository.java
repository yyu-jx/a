package com.superyoudi.kdbservice.repository;

import com.superyoudi.kdbservice.graph.KnowledgePointNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Sijayyx
 * @date 2026/7/16
 */
@Repository
public interface KnowledgePointRepository extends Neo4jRepository<KnowledgePointNode, String> {
    /**
     * 使用MERGE实现去重保存（以name为唯一标识）
     */
    @Query("MERGE (k:KnowledgePointNode {name: $name}) " +
            "ON CREATE SET " +
            "  k.description = $description, " +
            "ON MATCH SET " +
            "  k.description = CASE " +
            "    WHEN k.description IS NULL OR k.description = '' " +
            "    THEN $description " +
            "    ELSE k.description " +
            "  END" +
            "RETURN k")
    KnowledgePointNode saveOrUpdate(@Param("name") String name,
                                    @Param("description") String description);

    List<KnowledgePointNode> findAllByName(@Param("name") Iterable<String> name);
}
