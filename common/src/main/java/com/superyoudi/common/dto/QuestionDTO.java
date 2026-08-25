package com.superyoudi.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionDTO {
    private Integer id;

    private Long courseId;
    private Long teacherId;

    // 题目类型
    private String type;
    /// 题干
    private String content;
    // 答案
    private String answer;

    /// 题目分析（可选）
    private String analysis;

    /// 题目来源
    private String source;
    private String difficulty;

    /// 关联的知识点
    private List<String> knowledgeList;
}