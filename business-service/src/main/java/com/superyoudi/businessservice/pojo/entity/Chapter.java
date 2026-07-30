package com.superyoudi.businessservice.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 章实体
 * 对应数据库 chapters 章表
 */
@Data
public class Chapter {

    /**
     * 主键id
     */
    private Long id;

    /**
     * 关联课程id
     */
    private Long courseId;

    /**
     * 章节名称
     */
    private String name;

    /**
     * 章节简介
     */
    private String summary;

    /**
     * 同课程下章节排序
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 修改时间
     */
    private LocalDateTime updatedAt;
}