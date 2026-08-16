package com.superyoudi.businessservice.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Sijayyx
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseStudyOverviewVO {
    /**
     * 班级ID
     */
    private Long courseId;
    /**
     * 班级名称
     */
    private String courseName;
    /**
     * 平均进度
     */
    private double avgProgress;
    /**
     * 平均分百分比
     */
    private double avgScoreRate;
    /**
     * 提交率
     */
    private double avgSubmitRate;

    /**
     * 薄弱知识点列表，字符串数组。作为tag显示
     */
    private String weakestKnowledge;
}
