package com.superyoudi.businessservice.pojo.vo;

import lombok.Data;

@Data
public class StudyOverviewVO {
    /**
     * 学习课程总数
     */
    private Integer studyCourseTotal;

    /**
     * 平均总进度
     */
    private Double avgTotalProgress;

    /**
     * 平均交互次数
     */
    private Integer avgInteractionCount;

    /**
     * 平均任务得分
     */
    private Double avgTaskScore;

    /**
     * 平均知识点覆盖率
     */
    private Double avgKnowledgeCoverRate;

    /**
     * 平均任务完成率
     */
    private Double avgTaskCompletionRate;
}
