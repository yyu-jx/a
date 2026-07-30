package com.superyoudi.kdbservice.repository;

import com.superyoudi.kdbservice.graph.KnowledgeTextFragmentNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Sijayyx
 */
@Repository
public interface KnowledgeTextFragmentRepository extends Neo4jRepository<KnowledgeTextFragmentNode, String> {
}
