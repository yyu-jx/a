package com.superyoudi.businessservice.pojo.vo;

import lombok.Data;

/**
 * 薄弱知识点VO
 */
@Data
public class WeakKnowledgeVO {
    /**
     * 知识点名称
     */
    private String knowledgeName;
    /**
     * 知识点id（Neo4j节点id）
     */
    private String knowledgeId;
    /**
     * 错误率 %
     */
    private Double errorRate;
}