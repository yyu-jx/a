package com.superyoudi.common.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 学生课程进度DTO
 */
@Data
public class StudentCourseProgress {

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 课程总学习进度
     */
    private BigDecimal totalProgress;

    /**
     * 互动参与次数
     */
    private Integer interactionCount;

    /**
     * 平均作业均分
     */
    private BigDecimal avgTaskScore;

    /**
     * 知识点覆盖度
     */
    private BigDecimal knowledgeCoverRate;

    /**
     * 任务完成度
     */
    private BigDecimal taskCompletionRate;
}