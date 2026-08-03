package com.superyoudi.aiservice.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Sijayyx
 */
@Data
@AllArgsConstructor
public class LearningPathDTO {
    public Long courseId;
    public String courseName;
    public Long learningPathId;
    public Long currentPathNodeId;
    public String currentNodeName;
    public String currentStage;
}
