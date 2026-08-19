package com.superyoudi.businessservice.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskQuestionDTO {
    private Long id;
    private Long teacherId;
    private BigDecimal score;
    private String type;
    private String content;
    // 数据库原始JSON字符串
    private String answer;
}