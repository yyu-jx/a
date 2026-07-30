package com.superyoudi.businessservice.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 任务实体
 * 对应数据库 tasks 任务表
 */
@Data
public class Task {

    /**
     * 主键id
     */
    private Long id;

    /**
     * 所属课程ID
     */
    private Long courseId;

    /**
     * 布置者的用户ID
     */
    private Long assignerId;

    /**
     * 任务标题
     */
    private String title;

    /**
     * 任务说明
     */
    private String description;

    /**
     * 任务类型
     * homework 作业 | exam 考试 | qna 问答
     */
    private String type;

    /**
     * 任务总分
     */
    private Integer totalScore;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 截止时间
     */
    private LocalDateTime deadline;

    /**
     * 任务状态
     * draft 草稿 | published 已发布 | closed 已关闭
     */
    private String status;

    /**
     * 提交后策略
     * auto_all AI自动评分所有题目(客观+主观)
     * auto_objective 仅自动评分客观题
     * manual 教师完全手动评分
     */
    private String afterSubmitStrategy;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 修改时间
     */
    private LocalDateTime updatedAt;
}