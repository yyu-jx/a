package com.superyoudi.businessservice.pojo.vo;

import lombok.Data;

@Data
public class StudentTaskOverviewVO {
    /**
     * 待完成任务数量
     */
    private Integer todoTaskCount;
    /**
     * 已完成任务数量
     */
    private Integer finishedTaskCount;
    /**
     * 整体平均正确率 0~1
     */
    private Double averageAccuracy;
}