package com.superyoudi.businessservice.service;

import com.superyoudi.businessservice.pojo.dto.LoginResponse;

/**
 * 登录
 */
public interface AuthService {
    LoginResponse login(String username, String password);
}
