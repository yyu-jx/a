package com.superyoudi.businessservice.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StudentCourseProgressVO {
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
