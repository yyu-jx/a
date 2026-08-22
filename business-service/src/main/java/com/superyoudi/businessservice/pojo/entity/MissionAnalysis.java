package com.superyoudi.businessservice.pojo.entity;

import lombok.Data;

@Data
public class MissionAnalysis {

    private Long id;
    private Long taskId;
    private Long totalSubmitted;
    private Double avgScore;
    private Double maxScore;
    private Double minScore;
    private Double avgTimeSpent;


}