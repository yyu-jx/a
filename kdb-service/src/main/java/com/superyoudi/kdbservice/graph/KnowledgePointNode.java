package com.superyoudi.kdbservice.graph;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;

/**
 * @author Sijayyx
 * @date 2026/7/16
 */
@Data
@Node
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"relatedToKnowledgePoints", "knowledgeTextFragments"})
public class KnowledgePointNode {

    @Id
    private String name;
    private String description;
    private Long documentId;

    @Relationship(type = "RELATED_TO")
    private List<KnowledgePointRelation> relatedToKnowledgePoints;
    @Relationship(type = "CONTAINS", direction = Relationship.Direction.OUTGOING)
    private List<KnowledgeTextFragmentNode> knowledgeTextFragments;


    public KnowledgePointNode(String name, String description) {
        this.name = name;
        this.description = description;
    }

}
