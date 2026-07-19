package org.example.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.blog.dto.request.PageRequest;
import org.example.blog.dto.request.UpdateUserRequest;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface UserService extends IService<User> {
    void updateProfile(UpdateUserRequest request);

    String uploadAvatar(MultipartFile file);

    // TODO 用户端 查看个人信息 查看他人个人信息
    User getUser();

    void checkAdmin();

    void adminDeleteUserById(Long id);

    PageResponse<User> pageUser(PageRequest request);
}