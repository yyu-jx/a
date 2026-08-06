package com.superyoudi.businessservice.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 节实体
 * 对应数据库 sections 节表
 */
@Data
@AllArgsConstructor
public class Section {

    /**
     * 主键id
     */
    private Long id;

    /**
     * 关联章id
     */
    private Long chapterId;

    /**
     * 小节名称
     */
    private String name;

    /**
     * 小节简介
     */
    private String summary;

    /**
     * 同章节下小节排序
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