package com.superyoudi.common.dto;// TeacherCourseAnalysisVO.java
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TeacherHomeAnalysisDTO {
    private Long courseId;
    private String courseName;
    private Integer totalStudents;
    private BigDecimal avgScoreRate;
    private BigDecimal avgProgress;
    private Integer taskCount;
    private BigDecimal submissionRate;
    private Integer alertStudentCount;
    private String weakestKnowledge;
    private String weakestKnowledgeName;
}