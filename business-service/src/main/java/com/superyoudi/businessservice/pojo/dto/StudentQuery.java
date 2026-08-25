package com.superyoudi.businessservice.pojo.dto;

import lombok.Data;

@Data
public class StudentQuery {
    private Long courseId;
    private Long taskId;
    private Long pageNum;
    private Long pageSize;
    private String keyword;
    private String status;
}