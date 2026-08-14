package com.superyoudi.businessservice.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 课程列表页顶部的统计信息
 *
 * @author Sijayyx
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseStatisticsVO {
    private Integer courseTotal;
    private Integer studentTotal;
    private Double avgProgressTotal;
}
