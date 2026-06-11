package org.example.blog.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.example.blog.dto.request.ArticleRequest;
import org.example.blog.entity.Article;
import org.example.blog.service.ArticleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/article")
@RequiredArgsConstructor // 采用 @RequiredArgsConstructor 构造器的注解简化了注入
public class ArticleController {

    private final ArticleService articleService;

    @PostMapping
    public String createArticle(@Valid @RequestBody ArticleRequest articleRequest){
        Long userId = UserContext.get();
        articleService.createArticle(userId,articleRequest.getTitle(),articleRequest.getContent());
        return "发布成功";
    }

    @GetMapping
    public List<Article> getAllArticle(){
        return articleService.getAllArticle();
    }

    @GetMapping("/{id}")
    public Article getArticleById(@PathVariable Long id){
        return articleService.getArticleById(id);
    }
}
