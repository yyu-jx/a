package com.superyoudi.businessservice.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 学生提交实体
 * 对应数据库 student_submissions 学生提交表
 */
@Data
public class StudentSubmission {

    /**
     * 主键id
     */
    private Long id;

    /**
     * 任务id
     */
    private Long taskId;

    /**
     * 学生id
     */
    private Long studentId;

    /**
     * 提交状态
     * pending 已查看 | submitted 已提交 | passed 已批改 | returned 打回 | draft 草稿
     */
    private String status;

    /**
     * 提交时间
     */
    private LocalDateTime submittedTime;

    /**
     * 批改时间
     */
    private LocalDateTime gradedTime;

    /**
     * 总得分
     */
    private Integer totalScore;

    /**
     * 教师评语
     */
    private String comment;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}