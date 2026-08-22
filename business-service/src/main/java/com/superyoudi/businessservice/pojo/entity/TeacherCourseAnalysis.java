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
    private Long totalStudents;
    private Double avgScoreRate;
    private Double avgProgress;
    private Long taskCount;
    private Double submissionRate;
    private Long alertStudentCount;
    private String weakestKnowledge;
    private LocalDateTime updatedAt;
}
