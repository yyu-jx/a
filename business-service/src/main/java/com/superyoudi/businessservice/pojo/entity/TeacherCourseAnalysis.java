package com.superyoudi.businessservice.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 教师课程分析表
 *
 * @author Sijayyx
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
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
