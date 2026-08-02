package com.superyoudi.businessservice.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程实体
 * 对应数据库 课程表 courses
 */
@Data
public class CoursesVO {

    private Long id;

    /** 课程名称 */
    private String name;

    /** 课程描述 */
    private String description;

    /** 封面图 */
    private String coverUrl;

    /** 学分 */
    private Integer credit;

    /** 课程状态 opening正在上课 | archived已结课 */
    private String status;

    private String teacherName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
