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
 * 知识点节点
 * <p>
 * 以name为唯一标识。
 * <p>
 * 两个关系节点不会被包括在哈希计算中以避免栈溢出。
 *
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
    private Long documentId; // TODO 要不要删了它？

    /// 相关的知识点
    @Relationship(type = "RELATED_TO")
    private List<KnowledgePointRelation> relatedToKnowledgePoints;

    // 抽取出此知识点的多个原始文本
    @Relationship(type = "CONTAINS", direction = Relationship.Direction.OUTGOING)
    private List<KnowledgeTextFragmentNode> knowledgeTextFragments;


    public KnowledgePointNode(String name, String description) {
        this.name = name;
        this.description = description;
    }

}
