package com.superyoudi.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 学生知识点掌握情况表
 * student_knowledge_masteries
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentKnowledgeMastery {
    /**
     * 主键id
     */
    private Long id;

    /**
     * 学生用户ID
     */
    private Long studentId;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 知识点Neo4j唯一id
     */
    private String knowledgeId;

    /**
     * 知识点名称 数据库不存在，查询映射使用
     */
    private String knowledgeName;

    /**
     * 掌握率
     */
    private Double masterRate;

    /**
     * 答题正确次数
     */
    private Integer questionCorrectCount;

    /**
     * 答题总次数
     */
    private Integer questionTotalCount;

    /**
     * 【SQL查询字段】错误率，数据库不存在，查询映射使用
     */
    private Double errorRate;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}