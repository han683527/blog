package org.example.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.blog.entity.User;

public interface UserService extends IService<User> {
    void register(String email,String password,String nickname);

    User login(String email, String password);
}
