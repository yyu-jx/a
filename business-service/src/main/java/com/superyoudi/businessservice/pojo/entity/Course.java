package com.superyoudi.businessservice.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程实体
 * 对应数据库 课程表 courses
 */
@Data
public class Course {

    private Long id;

    /**
     * 课程名称
     */
    private String name;

    /**
     * 课程数据关联ID
     */
    private Long courseDataId;

    /**
     * 课程描述
     */
    private String description;

    /**
     * 封面图
     */
    private String coverUrl;

    /**
     * 学分
     */
    private Integer credit;

    /**
     * 课程状态 opening正在上课 | archived已结课
     */
    private String status;

    /**
     * 任课教师id，外键关联users.id
     */
    private Long teacherId;


    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
