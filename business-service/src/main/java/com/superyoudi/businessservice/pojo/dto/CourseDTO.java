package com.superyoudi.businessservice.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Sijayyx
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseDTO {
    private Long id;
    private String name;
    private String description;
    private Long teacherId;
    private Long courseDataId;
    private String coverUrl;
    private Integer credit;
}
