package com.superyoudi.businessservice.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务表单
 *
 * @author Sijayyx
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskDTO {

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
     * 关联的题目
     */
    private List<QuestionRelInfo> questionRelInfos;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QuestionRelInfo {
        private Long id; // 关联表ID，可为null
        private Long questionId;
        private Integer questionSort;
        private Integer score;
    }

}
