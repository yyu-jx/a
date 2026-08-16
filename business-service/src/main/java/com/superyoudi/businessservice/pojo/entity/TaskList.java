package com.superyoudi.businessservice.pojo.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TaskList {
    private Long taskId;
    private Long courseId;
    private String courseName;
    private String title;
    private String description;
    private String type;
    private BigDecimal totalScore;
    private LocalDateTime startTime;
    private LocalDateTime deadline;
    // 任务本身状态 draft / published / closed
    private String taskStatus;
    // 学生提交状态：draft / submitted / passed / returned / pending；无提交为 null
    private String submitStatus;
    // 学生得分，没有提交为 null
    private BigDecimal userScore;
}