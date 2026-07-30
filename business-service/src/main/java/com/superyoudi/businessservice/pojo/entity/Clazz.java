package com.superyoudi.businessservice.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 班级实体
 * 对应数据库 班级表 classes
 */
@Data
public class Clazz {

    private Long id;

    /** 班级名称 */
    private String name;

    /** 创建时间 */
    private LocalDateTime createAt;
}
