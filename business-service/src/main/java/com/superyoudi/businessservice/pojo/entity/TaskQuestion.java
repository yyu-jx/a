package com.superyoudi.businessservice.pojo.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TaskQuestion {
    private Long id;
    private Long teacherId;
    private BigDecimal score;
    private String type;
    private String content;
    // 数据库原始JSON字符串
    private String answer;
}