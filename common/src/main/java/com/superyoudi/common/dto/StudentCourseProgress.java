package com.superyoudi.common.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 学生课程进度DTO
 */
@Data
public class StudentCourseProgress {

    private Long id;

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
    private double totalProgress;

    /**
     * 互动参与次数
     */
    private Long interactionCount;

    /**
     * 平均作业均分
     */
    private double avgTaskScore;

    /**
     * 知识点覆盖度
     */
    private double knowledgeCoverRate;

    /**
     * 任务完成度
     */
    private double taskCompletionRate;
}