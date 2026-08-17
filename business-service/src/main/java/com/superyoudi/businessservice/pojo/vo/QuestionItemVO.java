package com.superyoudi.businessservice.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

// 单道题目信息
@Data
public class QuestionItemVO {
    private Long id;
    private Long teacherId;
    private BigDecimal score;
    private String type;
    private String content;
    // 选项列表，对应接口文档的 answer
    private List<QuestionOptionVO> answer;
}