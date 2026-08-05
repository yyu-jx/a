package com.superyoudi.businessservice.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体
 * 对应数据库 用户表 users
 */
@Data
public class User {


    private Long id;

    /** 用户名 或 学号 */
    private String username;

    /** 真实姓名 */
    private String realname;

    /** 密码哈希值 */
    private String password;

    /** 角色 student学生 | teacher教师 | researcher研究员 */
    private String role;

    /** 头像url */
    private String avatarUrl;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 修改时间 */
    private LocalDateTime updatedAt;
}
