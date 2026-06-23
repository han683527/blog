package org.example.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.blog.dto.request.LoginRequest;
import org.example.blog.dto.request.RegisterRequest;
import org.example.blog.entity.User;

public interface UserService extends IService<User> {
    void register(RegisterRequest request);

    User login(LoginRequest request);
}
