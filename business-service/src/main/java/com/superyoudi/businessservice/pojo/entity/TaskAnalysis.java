package com.superyoudi.businessservice.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author Sijayyx
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskAnalysis {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 提交人数
     */
    private Integer totalSubmitted;

    /**
     * 平均分
     */
    private Double avgScore;

    /**
     * 最高分
     */
    private Integer maxScore;

    /**
     * 最低分
     */
    private Integer minScore;

    /**
     * 平均耗时(秒)
     */
    private Integer avgTimeSpent;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}