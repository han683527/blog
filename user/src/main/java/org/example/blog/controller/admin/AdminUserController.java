package org.example.blog.controller.admin;

import lombok.RequiredArgsConstructor;
import org.example.blog.dto.request.PageRequest;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.dto.response.Result;
import org.example.blog.entity.User;
import org.example.blog.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/user")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @DeleteMapping("/{id}")
    public Result<String> adminDeleteUserById(@PathVariable Long id){
        userService.adminDeleteUserById(id);
        return Result.success("删除成功");
    }

    @PostMapping("/list")
    public Result<PageResponse<User>> adminPageUser(PageRequest request){
        return Result.success(userService.adminPageUser(request));
    }
}
