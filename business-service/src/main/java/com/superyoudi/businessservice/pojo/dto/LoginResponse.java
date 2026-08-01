package com.superyoudi.businessservice.pojo.dto;

import lombok.Data;


@Data
public class LoginResponse {

    // 用户ID
    private Long userId;

    // 用户名
    private String username;

    // 真实姓名
    private String realname;

    // 角色
    private String role;

    // 头像URL
    private String avatarUrl;

    // 登录凭证
    private String token;
}
