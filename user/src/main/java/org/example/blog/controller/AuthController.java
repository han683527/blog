package org.example.blog.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.blog.dto.request.LoginRequest;
import org.example.blog.dto.request.RefreshTokenRequest;
import org.example.blog.dto.request.RegisterRequest;
import org.example.blog.dto.response.LoginResponse;
import org.example.blog.dto.response.Result;
import org.example.blog.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody RegisterRequest request){
        authService.register(request);
        return Result.success("注册成功");
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request){
        return Result.success(authService.login(request));
    }

    @PostMapping("/refresh")
    public Result<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request){
        return Result.success(authService.refresh(request));
    }

    @PostMapping("/logout")
    public Result<String> logout(@RequestHeader("Authorization") String authHeader) {
        authService.logout(authHeader);
        return Result.success("登出成功");
    }

    @PostMapping("/code")
    public Result<String> sendCode(@RequestParam String email){
        authService.sendCode(email);
        return Result.success("验证码已发送");
    }
}
