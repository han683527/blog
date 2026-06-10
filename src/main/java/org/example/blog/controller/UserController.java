package org.example.blog.controller;

import jakarta.validation.Valid;
import org.example.blog.dto.request.LoginRequest;
import org.example.blog.dto.request.RegisterRequest;
import org.example.blog.dto.response.LoginResponse;
import org.example.blog.entity.User;
import org.example.blog.service.UserService;
import org.example.blog.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    private UserService userService;

    //@Autowired
    public UserController(UserService userService){
        this.userService = userService;
    }

    //@RequestBody 注解处理前端发来的 JSON 字符串-> Java 对象
    //@Valid 触发校验,进行对象是否为空等判断
    @PostMapping("/register")
    public String register(@Valid @RequestBody RegisterRequest registerRequest){
        userService.register(registerRequest.getEmail(),registerRequest.getPassword(),registerRequest.getNickname());
        return "注册成功";
    }

    @PostMapping("login")
    public LoginResponse login(@Valid @RequestBody LoginRequest loginRequest){
        User user = userService.login(loginRequest.getEmail(),loginRequest.getPassword());
        String token = JwtUtil.generateToken(user.getId(),user.getEmail());
        return new LoginResponse(token,user.getEmail(),user.getPassword(),user.getNickname());
    }
}
