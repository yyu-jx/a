package com.superyoudi.businessservice.controller.auth;

import com.superyoudi.businessservice.pojo.dto.LoginRequest;
import com.superyoudi.businessservice.pojo.dto.LoginResponse;
import com.superyoudi.businessservice.pojo.entity.Result;
import com.superyoudi.businessservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录
 * @author at
 */
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 登录
     * @param loginRequest 用户名 + 密码
     * @return 登录结果
     */
    @PostMapping("/user/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest.getUsername(), loginRequest.getPassword());
        if (response == null) {
            return Result.error("用户名或密码错误");
        }
        return Result.success(response);
    }
}
