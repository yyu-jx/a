package com.superyoudi.businessservice.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 学生单题答题实体
 * 对应数据库 student_answers 学生单题答题表
 */
@Data
public class StudentAnswer {

    /**
     * 主键id
     */
    private Long id;

    /**
     * 所属的提交ID
     */
    private Long submissionId;

    /**
     * 题目ID
     */
    private Long questionId;

    /**
     * 学生答案，JSON字符串
     */
    private String answer;

    /**
     * 单题得分
     */
    private Integer score;

    /**
     * 单题教师评语
     */
    private String comment;

    /**
     * 答题耗时，单位秒
     */
    private Integer timeSpent;

    /**
     * 提交前的修改次数
     */
    private Integer attemptCount;

    /**
     * 求助次数
     */
    private Integer helpRequestCount;

    /**
     * 错误类型，null代表无错误
     */
    private String errorType;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}