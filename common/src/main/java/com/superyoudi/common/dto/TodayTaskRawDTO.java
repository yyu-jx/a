package com.superyoudi.common.dto;

import lombok.Data;

@Data
public class TodayTaskRawDTO {
    private Long courseId;
    private Long pathId;
    private String knowledgeId;
    private String status;
    private String courseName;
}