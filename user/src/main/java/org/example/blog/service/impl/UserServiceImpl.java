package org.example.blog.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.example.blog.dto.request.PageRequest;
import org.example.blog.dto.request.UpdateUserRequest;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.entity.User;
import org.example.blog.exception.BadRequestException;
import org.example.blog.exception.ForbiddenException;
import org.example.blog.exception.NotFoundException;
import org.example.blog.mapper.UserMapper;
import org.example.blog.service.UserService;
import org.example.blog.util.UserContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Value("${upload.path}")
    private String uploadPath;

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

    @Override
    public User getUser() {
        return this.getById(UserContext.get());
    }

    @Override
    public User getUserById(Long id) {
        return this.getOptById(id).orElseThrow(() -> new NotFoundException("用户不存在"));
    }

    @Override
    public void checkAdmin(){
        User user = this.getById(UserContext.get());
        if(!"admin".equals(user.getRole())){
            throw new ForbiddenException("权限不足");
        }
    }

    @Override
    public void adminDeleteUserById(Long id){
        checkAdmin();
        User user = this.getOptById(id)
                .orElseThrow(() -> new NotFoundException("用户不存在"));
        this.removeById(id);
    }

    @Override
    public PageResponse<User> adminPageUser(PageRequest request){
        checkAdmin();
        Page<User> p = this.page(new Page<>(request.getPage(),request.getSize()));
        PageResponse<User> response = new PageResponse<>();
        response.setTotal(p.getTotal());
        response.setList(p.getRecords());
        return response;
    }
}
