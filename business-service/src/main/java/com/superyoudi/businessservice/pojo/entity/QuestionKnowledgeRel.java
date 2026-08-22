package com.superyoudi.businessservice.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Sijayyx
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuestionKnowledgeRel {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 题目ID
     */
    private Long questionId;

    /**
     * Neo4j知识点ID
     */
    private String knowledgeNode;

    /**
     * 重要程度排序（数值越小越重要）
     */
    private Integer sort;

}
