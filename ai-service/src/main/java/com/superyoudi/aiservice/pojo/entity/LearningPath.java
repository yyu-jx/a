package com.superyoudi.aiservice.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 学习路径实体
 * 对应数据库 learning_paths 学习路径表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LearningPath {

    /**
     * 主键id
     */
    private Long id;

    /**
     * 学生id
     */
    private Long studentId;

    /**
     * 课程id
     */
    private Long courseId;

    /**
     * 当前正在进行的路径节点id，null表示未开始
     */
    private Long currentNodeId;

    /**
     * 当前正在进行的教学阶段，用于动态注入提示词。introducing 引入 | explaining 讲解 | testing 测验 | finished 结束(保留字段)
     */
    private String currentStage;

    /**
     * AI生成的附加描述，可选
     */
    private String additionalInfo;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}