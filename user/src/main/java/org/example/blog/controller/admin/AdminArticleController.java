package org.example.blog.controller.admin;

import lombok.RequiredArgsConstructor;
import org.example.blog.dto.request.ArticleRequest;
import org.example.blog.dto.request.ArticleSearchRequest;
import org.example.blog.dto.response.ArticleResponse;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.dto.response.Result;
import org.example.blog.service.ArticleService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/article")
@RequiredArgsConstructor
public class AdminArticleController {

    private final ArticleService articleService;

    @DeleteMapping("/{id}")
    public Result<String> adminDeleteArticleById(@PathVariable Long id) {
        articleService.adminDeleteArticleById(id);
        return Result.success("删除成功");
    }

    @PostMapping("/list")
    public Result<PageResponse<ArticleResponse>> adminPageArticle(@RequestBody ArticleSearchRequest request) {
        return Result.success(articleService.adminPageArticle(request));
    }

    @PutMapping
    public Result<String> adminUpdateArticle(@RequestBody ArticleRequest request){
        articleService.adminUpdateArticle(request);
        return Result.success("修改成功");
    }
}

