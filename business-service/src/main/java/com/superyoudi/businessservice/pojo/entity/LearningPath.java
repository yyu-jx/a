package com.superyoudi.businessservice.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 学习路径实体
 * 对应数据库 learning_paths 学习路径表
 */
@Data
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