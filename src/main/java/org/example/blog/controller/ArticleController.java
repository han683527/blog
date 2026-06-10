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
public class ArticleController {

    private ArticleService articleService;

    public ArticleController(ArticleService articleService){
        this.articleService = articleService;
    }

    @PostMapping
    public String createArticle(@Valid @RequestBody ArticleRequest articleRequest, HttpServletRequest httpRequest){
        Long userId = (Long) httpRequest.getAttribute("userId");
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
