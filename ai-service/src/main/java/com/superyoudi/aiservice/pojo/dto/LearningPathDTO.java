package com.superyoudi.aiservice.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Sijayyx
 */
@Data
@AllArgsConstructor
public class LearningPathDTO {
    private Long courseId;
    private String courseName;
    private Long learningPathId;
    private Long currentPathNodeId;
    private String currentNodeName;
    private String currentStage;
}
