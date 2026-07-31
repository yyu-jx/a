package com.superyoudi.businessservice.pojo.vo;

import lombok.Data;

/**
 * 获取上次学习记录返回VO
 */
@Data
public class LastStudyRecordVO {

    /**
     * 学习路径主键，前端跳转携带
     */
    private Long pathId;

    /**
     * 课程id
     */
    private Long courseId;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 当前知识点标题
     */
    private String currentKnowledgeTitle;

    /**
     * 剩余未完成知识点数量
     */
    private Integer remainingUntestedCount;

    /**
     * 当前知识点Neo4j ID，前端跳转携带
     */
    private String currentKnowledgeId;

    /**
     * 最近学习时间
     */
    private String lastStudyTime;
}