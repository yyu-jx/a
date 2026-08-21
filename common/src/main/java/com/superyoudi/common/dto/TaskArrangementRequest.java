package com.superyoudi.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author Sijayyx
 */
@AllArgsConstructor
@Builder
@Data
public class TaskArrangementRequest {
    private Long assignerId;
    private Long courseId;
    private List<String> knowledgePoints;
    private String title;
    private String description;
    private String type;
    private Map<String, Integer> questionConfig;
    private Integer totalScore;
}
