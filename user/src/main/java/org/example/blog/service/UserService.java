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

    // 个人信息查看
    User getUser();

    // 查看他人信息
    User getUserById(Long id);

    void checkAdmin();

    void adminDeleteUserById(Long id);

    PageResponse<User> pageUser(PageRequest request);
}