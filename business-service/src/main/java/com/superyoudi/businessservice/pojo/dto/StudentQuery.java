package com.superyoudi.businessservice.pojo.dto;

import lombok.Data;

@Data
public class StudentQuery {
    private Integer courseId;
    private Integer taskId;
    private Integer pageNum;
    private Integer pageSize;
    private String keyword;
    private String status;
}