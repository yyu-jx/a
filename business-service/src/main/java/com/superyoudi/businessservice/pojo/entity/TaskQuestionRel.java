package com.superyoudi.businessservice.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务题目关联实体
 * 对应数据库 task_question_rel 任务题目关联表
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class TaskQuestionRel {

    /**
     * 主键id
     */
    private Long id;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 题目ID
     */
    private Long questionId;

    /**
     * 单题分值
     */
    private Integer score;

    /**
     * 题号
     */
    private Integer questionSort;
}
