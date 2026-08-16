package com.superyoudi.businessservice.pojo.vo;

import lombok.Data;

import java.util.List;

// 外层返回：任务信息 + 题目列表
@Data
public class TaskQuestionVO {
    private Long courseId;
    private String taskType;
    private String taskTitle;
    private List<QuestionItemVO> questionList;
}