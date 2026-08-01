package com.superyoudi.businessservice.pojo.dto;

import lombok.Data;

/**
 * 登录请求体
 */
@Data
public class LoginRequest {

    // 用户名
    private String username;

    // 密码
    private String password;
}
