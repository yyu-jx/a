package com.superyoudi.kdbservice.graph;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

/**
 * @author Sijayyx
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@RelationshipProperties
@EqualsAndHashCode(exclude = {"targetNode"})
public class KnowledgePointRelation {
    @RelationshipId
    private String id;

    private String relationId;

    /// 知识点的关系类型。前三个是最重要的，`RELATED_TO`在无显要关系时使用。
    ///
    /// | **关系类型** | **含义** | **方向** | **示例** |
    /// | --- | --- | --- | --- |
    /// | `IS_A` | 子类/实例关系 | 有向 | `(快速排序)-\\\[:IS_A\\\]-\\\>(排序算法)` |
    /// | `HAS_A` | 组成/包含关系 | 有向 | `(CPU)-\\\[:HAS_PART\\\]-\\\>(寄存器)` |
    /// | `PREREQUISITE_OF` | 前置依赖关系 | 有向 | `(极限)-\\\[:PREREQUISITE_OF\\\]-\\\>(导数)` |
    /// | `APPLIED_IN` | 应用/实践关系 | 有向 | `(贝叶斯定理)-\\\[:APPLIED_IN\\\]-\\\>(垃圾邮件过滤)` |
    /// | `SIMILAR_TO` | 相似/类比关系 | 无向 | `(栈)-\\\[:SIMILAR_TO\\\]-\\\>(队列)` |
    /// | `DIFFERENTIATES_FROM` | 区分/对比关系 | 有向 | `(数组)-\\\[:DIFFERENTIATES_FROM\\\]-\\\>(链表)` |
    /// | `RELATED_TO` | 一般关联关系 | 无向 | `(量子力学)-\\\[:RELATED_TO\\\]-\\\>(相对论)` |
    private String relationType;

    @TargetNode
    private KnowledgePointNode targetNode;

    public KnowledgePointRelation(KnowledgePointNode targetNode, String relationType) {
        this.targetNode = targetNode;
        this.relationType = relationType;
    }

    public void setRelationId(String source, String target) {
        this.relationId = source + "_" + relationType + "_" + target;
    }
}
