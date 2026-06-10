package org.example.blog.service;

import org.example.blog.entity.User;

public interface UserService {
    void register(String email,String password,String nickname);

    User login(String email, String password);
}
