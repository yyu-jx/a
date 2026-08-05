package com.superyoudi.businessservice.pojo.vo;

import lombok.Data;

/**
 * 今日学习任务VO
 */
@Data
public class TodayTaskVO {
    /** 任务标题：课程名称 - 知识点名称 */
    private String taskTitle;
    /** 课程id */
    private Integer courseId;
    /** 知识点id（Neo4j） */
    private String knowledgeId;
    /** 知识点状态 untested / proceeding，不会出现completed */
    private String knowledgeStatus;
}