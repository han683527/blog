package org.example.blog.controller;

import lombok.RequiredArgsConstructor;
import org.example.blog.dto.request.PageRequest;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.dto.response.Result;
import org.example.blog.service.LikeService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/like")
@RequiredArgsConstructor
public class LikeController {
    public final LikeService likeService;

    @PostMapping("/{id}")
    public Result<String> toggle(@PathVariable(value = "id") Long articleId) {
        likeService.toggle(articleId);
        return Result.success("操作成功");
    }

    @PostMapping("list")
    public Result<PageResponse> pageMyLike(@RequestBody PageRequest request) {
        return Result.success(likeService.pageMyLike(request));
    }
}
