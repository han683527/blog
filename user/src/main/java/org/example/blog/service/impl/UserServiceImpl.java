package org.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.blog.dto.request.LoginRequest;
import org.example.blog.dto.request.RefreshTokenRequest;
import org.example.blog.dto.request.RegisterRequest;
import org.example.blog.dto.response.LoginResponse;
import org.example.blog.entity.User;
import org.example.blog.exception.BadRequestException;
import org.example.blog.exception.NotFoundException;
import org.example.blog.mapper.UserMapper;
import org.example.blog.service.UserService;
import org.example.blog.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public void register(RegisterRequest request) {
        String email = request.getEmail();
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>();
        wrapper.eq(User::getEmail, email);
        if (this.count(wrapper) > 0) {
            throw new BadRequestException("邮箱已被注册");
        }

        //2.密码加密
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(); // BCrypt 哈希(每次加密结果不同)
        String encodePassword = passwordEncoder.encode(request.getPassword());

        //3.创建用户并保存

        User user = new User();
        user.setEmail(email);
        user.setPassword(encodePassword);
        user.setNickname(request.getNickname());
        this.save(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        //1.查看数据库有没有这个邮箱
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>();
        wrapper.eq(User::getEmail, request.getEmail());
        User user = this.getOne(wrapper);
        //找到邮箱后获取
        if (user == null) {
            throw new NotFoundException("邮箱未注册");
        }

        //2.检查密码是否匹配
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("密码错误");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());
        LoginResponse response = new LoginResponse(accessToken,refreshToken,user.getEmail());
        return response;
    }

    public LoginResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        Long userId = jwtUtil.validateAndGetUserId(refreshToken);
        jwtUtil.blacklistToken(refreshToken);

        User user = this.getById(userId);
        if (user == null) {
            throw new NotFoundException("用户不存在");
        }

        String newAccessToken = jwtUtil.generateAccessToken(user.getId(),user.getEmail());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId());
        LoginResponse response = new LoginResponse(newAccessToken,newRefreshToken, user.getEmail());
        return response;
    }

    public void logout(String authHeader) {
        if(authHeader != null && authHeader.startsWith("Bearer ")) {
            jwtUtil.blacklistToken(authHeader.substring(7));
        }
    }
}
