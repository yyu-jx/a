package com.superyoudi.businessservice.service.impl;

import com.superyoudi.businessservice.mapper.UserMapper;
import com.superyoudi.businessservice.pojo.dto.LoginResponse;
import com.superyoudi.businessservice.pojo.entity.Users;
import com.superyoudi.businessservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 登录
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;

    @Override
    public LoginResponse login(String username, String password) {
        // 根据用户名查询用户
        Users user = userMapper.selectByUsername(username);
        if (user == null) {
            return null;
        }

        // 校验密码
        if (!user.getPassword().equals(password)) {
            return null;
        }

        // 组装响应
        LoginResponse response = new LoginResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRealname(user.getRealname());
        response.setRole(user.getRole());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setToken(UUID.randomUUID().toString());

        return response;
    }
}
