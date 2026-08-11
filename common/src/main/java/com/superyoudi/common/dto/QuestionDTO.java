package com.superyoudi.common.dto;

import lombok.Data;


@Data
public class QuestionDTO {
    private Integer id;
    private String content;
    private String answer;
}