package org.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.example.blog.dto.request.LoginRequest;
import org.example.blog.dto.request.RefreshTokenRequest;
import org.example.blog.dto.request.RegisterRequest;
import org.example.blog.dto.request.UpdateUserRequest;
import org.example.blog.dto.response.LoginResponse;
import org.example.blog.entity.User;
import org.example.blog.exception.BadRequestException;
import org.example.blog.exception.NotFoundException;
import org.example.blog.mapper.UserMapper;
import org.example.blog.service.UserService;
import org.example.blog.util.JwtUtil;
import org.example.blog.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final JwtUtil jwtUtil;

    private final StringRedisTemplate redisTemplate;

    private final JavaMailSender javaMailSender;

    @Value("${upload.path}")
    private String uploadPath;

    @Override
    public void register(RegisterRequest request) {
        // 验证码校验
        String cachedCode = redisTemplate.opsForValue().get("code:" + request.getEmail());
        if(cachedCode == null || !cachedCode.equals(request.getCode())) {
            throw new BadRequestException("验证码错误或已过期");
        }

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

    @Override
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

    @Override
    public void logout(String authHeader) {
        if(authHeader != null && authHeader.startsWith("Bearer ")) {
            jwtUtil.blacklistToken(authHeader.substring(7));
        }
    }

    @Override
    public void sendCode(String email) {
        // 1.生成 6 位随机码
        String code = String.format("%06d", new Random().nextInt(999999));

        // 2. 存 Redis (1分钟有效)
        redisTemplate.opsForValue().set("code:" + email,code,5, TimeUnit.MINUTES);

        // 3. 发邮件
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("1669096576@qq.com");
        message.setTo(email);
        message.setSubject("博客注册验证码");
        message.setText("您的验证码是: " + code + ". 5 分钟之内有效");
        javaMailSender.send(message);
    }

    public void updateProfile(UpdateUserRequest request) {
        Long userId = UserContext.get();
        User user = this.getById(userId);

        // 1.改昵称
        if(request.getNickname() != null && !request.getNickname().isEmpty()) {
            user.setNickname(request.getNickname());
        }

        // 2.改密码
        if(request.getOldPassword() != null && request.getNewPassword() != null) {
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            if(!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                throw new BadRequestException("旧密码错误");
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        this.updateById(user);
    }

    @Override
    public String uploadAvatar(MultipartFile file) {
        // 1. 校验文件类型
        String contentType = file.getContentType();
        if(contentType == null || !contentType.contains("image/")) {
            throw new BadRequestException("只能上传图片文件");
        }

        // 2. 生成文件名
        String suffix = "." + StringUtils.getFilenameExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + suffix;

        // 3. 保存文件
        try{
            Files.createDirectories(Paths.get(uploadPath));
            file.transferTo(new File(uploadPath, fileName));
        } catch (IOException e){
            throw new RuntimeException("上传失败", e);
        }

        // 4. 更新用户头像字段
        String url = "/upload/" +  fileName;
        User user = this.getById(UserContext.get());
        user.setAvatar(url);
        this.updateById(user);

        return url;
    }
}
