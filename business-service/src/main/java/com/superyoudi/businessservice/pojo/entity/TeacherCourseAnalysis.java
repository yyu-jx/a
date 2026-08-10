package com.superyoudi.businessservice.pojo.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 教师课程分析表
 *
 * @author Sijayyx
 */
@Data
public class TeacherCourseAnalysis {
    private Long id;
    private Long courseId;
    private Integer totalStudents;
    private BigDecimal avgScoreRate;
    private BigDecimal avgProgress;
    private Integer taskCount;
    private BigDecimal submissionRate;
    private Integer alertStudentCount;
    private String weakestKnowledge;
    private LocalDateTime updatedAt;
}
