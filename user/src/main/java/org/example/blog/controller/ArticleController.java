package org.example.blog.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.blog.dto.request.ArticleRequest;
import org.example.blog.dto.request.ArticleSearchRequest;
import org.example.blog.dto.response.ArticleResponse;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.dto.response.Result;
import org.example.blog.service.ArticleService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/article")
@RequiredArgsConstructor // 采用 @RequiredArgsConstructor 构造器的注解简化了注入
public class ArticleController {

    private final ArticleService articleService;

    @PostMapping
    public Result<String> createArticle(@Valid @RequestBody ArticleRequest request) {
        articleService.createArticle(request);
        return Result.success("发布成功");
    }

    @PostMapping("/list")
    public Result<PageResponse<ArticleResponse>> pageArticle(@RequestBody ArticleSearchRequest request) {
        return Result.success(articleService.pageArticle(request));
    }

    @GetMapping("/{id}")
    public Result<ArticleResponse> getArticleById(@PathVariable Long id) {
        return Result.success(articleService.getArticleById(id));
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteArticleById(@PathVariable Long id) {
        articleService.deleteArticleById(id);
        return Result.success("删除成功");
    }

    @PutMapping
    public Result<String> updateArticleById(@Valid @RequestBody ArticleRequest request) {
        articleService.updateArticleById(request);
        return Result.success("修改成功");
    }

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<String> uploadImage(MultipartFile file) {
        return Result.success(articleService.uploadImage(file));
    }
}