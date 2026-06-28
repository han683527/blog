package org.example.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.blog.dto.request.LoginRequest;
import org.example.blog.dto.request.RefreshTokenRequest;
import org.example.blog.dto.request.RegisterRequest;
import org.example.blog.dto.request.UpdateUserRequest;
import org.example.blog.dto.response.LoginResponse;
import org.example.blog.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface UserService extends IService<User> {
    void register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    LoginResponse refresh(RefreshTokenRequest request);

    void logout(String authHeader);

    void sendCode(String email);

    void updateProfile(UpdateUserRequest request);

    String uploadAvatar(MultipartFile file);

    void checkAdmin();

    void adminDeleteUserById(Long id);

    PageResponse<User> pageUser(PageRequest request);
}
