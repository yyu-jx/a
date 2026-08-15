package com.superyoudi.businessservice.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 章实体
 * 对应数据库 chapters 章表
 */
@Data
@AllArgsConstructor
public class Chapter {

    /**
     * 主键id
     */
    private Long id;

    /**
     * 关联课程数据id
     */
    private Long courseDataId;

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