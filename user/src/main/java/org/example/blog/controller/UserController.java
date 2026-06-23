package org.example.blog.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.blog.dto.request.LoginRequest;
import org.example.blog.dto.request.RefreshTokenRequest;
import org.example.blog.dto.request.RegisterRequest;
import org.example.blog.dto.response.LoginResponse;
import org.example.blog.dto.response.Result;
import org.example.blog.entity.User;
import org.example.blog.service.UserService;
import org.example.blog.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

//    public UserController(UserService userService){
//        this.userService = userService;
//    }

    //@RequestBody 注解处理前端发来的 JSON 字符串-> Java 对象
    //@Valid 触发校验,进行对象是否为空等判断
    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody RegisterRequest request){
        userService.register(request);
        return Result.success("注册成功");
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request){
        return Result.success(userService.login(request));
    }

    @PostMapping("/refresh")
    public Result<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request){
        return Result.success(userService.refresh(request));
    }

    @PostMapping("/logout")
    public Result<String> logout(@RequestHeader("Authorization") String authHeader) {
        userService.logout(authHeader);
        return Result.success("登出成功");
    }
}
