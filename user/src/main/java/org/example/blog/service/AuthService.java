package org.example.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.blog.dto.request.LoginRequest;
import org.example.blog.dto.request.RefreshTokenRequest;
import org.example.blog.dto.request.RegisterRequest;
import org.example.blog.dto.response.LoginResponse;
import org.example.blog.entity.User;

public interface AuthService extends IService<User> {
    void sendCode(String email);

    void register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    LoginResponse refresh(RefreshTokenRequest request);

    void logout(String authHeader);
}
