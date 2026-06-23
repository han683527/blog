package org.example.blog.service;

import org.example.blog.dto.request.LoginRequest;
import org.example.blog.dto.request.RegisterRequest;
import org.example.blog.entity.User;
import org.example.blog.exception.BadRequestException;
import org.example.blog.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class UserServiceTest {

    @Autowired
    private UserService userService;

    private RegisterRequest createRegisterReq(String email, String password, String nickname) {
        RegisterRequest req = new RegisterRequest();
        req.setEmail(email);
        req.setPassword(password);
        req.setNickname(nickname);
        return req;
    }

    private LoginRequest createLoginReq(String email, String password) {
        LoginRequest req = new LoginRequest();
        req.setEmail(email);
        req.setPassword(password);
        return req;
    }

    // ---------- 注册 ----------

    @Test
    void testRegister_Success() {
        userService.register(createRegisterReq("test@example.com", "123456", "测试用户"));

        User user = userService.login(createLoginReq("test@example.com", "123456"));
        assertNotNull(user);
        assertEquals("test@example.com", user.getEmail());
    }

    @Test
    void testRegister_EmailAlreadyExists() {
        userService.register(createRegisterReq("test@example.com", "123456", "用户1"));
        assertThrows(BadRequestException.class,
                () -> userService.register(createRegisterReq("test@example.com", "654321", "用户2")));
    }

    // ---------- 登录 ----------

    @Test
    void testLogin_Success() {
        userService.register(createRegisterReq("test@example.com", "123456", "测试用户"));

        User user = userService.login(createLoginReq("test@example.com", "123456"));
        assertNotNull(user);
    }

    @Test
    void testLogin_EmailNotFound() {
        assertThrows(NotFoundException.class,
                () -> userService.login(createLoginReq("notexist@example.com", "123456")));
    }

    @Test
    void testLogin_WrongPassword() {
        userService.register(createRegisterReq("test@example.com", "123456", "测试用户"));
        assertThrows(BadRequestException.class,
                () -> userService.login(createLoginReq("test@example.com", "wrong")));
    }
}
