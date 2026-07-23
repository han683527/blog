package org.example.blog.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.blog.dto.request.UpdateUserRequest;
import org.example.blog.dto.response.Result;
import org.example.blog.entity.User;
import org.example.blog.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/profile")
    public Result<String> updateProfile(@Valid @RequestBody UpdateUserRequest request){
        userService.updateProfile(request);
        return Result.success("更新成功");
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<String> uploadAvatar(MultipartFile file){
        return Result.success(userService.uploadAvatar(file));
    }

    @GetMapping("/info")
    public Result<User> getUser() {
        return Result.success(userService.getUser());
    }

    @GetMapping("/public/{id}")
    public Result<User> getUserById(@PathVariable Long id){
        return Result.success(userService.getUserById(id));
    }
}
