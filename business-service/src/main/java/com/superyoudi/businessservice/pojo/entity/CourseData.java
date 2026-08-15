package com.superyoudi.businessservice.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author Sijayyx
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseData {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 课程名称
     */
    private String name;

    /**
     * 课程的教材等信息
     */
    private String info;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}