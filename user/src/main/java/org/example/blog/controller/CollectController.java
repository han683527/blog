package org.example.blog.controller;

import lombok.RequiredArgsConstructor;
import org.example.blog.dto.request.ArticleSearchRequest;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.dto.response.Result;
import org.example.blog.service.CollectService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/collect")
@RequiredArgsConstructor
public class CollectController {

    private final CollectService collectService;

    @PostMapping("/{id}")
    public Result<String> toggle(@PathVariable(value = "id") Long articleId) {
        collectService.toggle(articleId);
        return Result.success("操作成功");
    }


    @PostMapping("list")
    public Result<PageResponse> pageMyCollect(@RequestBody ArticleSearchRequest request) {
        return Result.success(collectService.pageMyCollect(request));
    }
}
