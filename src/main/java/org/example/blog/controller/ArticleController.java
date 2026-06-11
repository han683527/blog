package org.example.blog.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.blog.dto.request.ArticleRequest;
import org.example.blog.entity.Article;
import org.example.blog.service.ArticleService;
import org.example.blog.util.UserContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

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

//    @DeleteMapping
//    public String deleteAllArticle(){
//        articleService.deleteAllArticle();
//        return "删除成功";
//    }

    @DeleteMapping("/{id}")
    public String deleteArticleById(@PathVariable Long id){
        articleService.deleteArticleById(id);
        return "删除成功";
    }

    @PutMapping("/{id}")
    public String updateArticleById(@PathVariable Long id ,
                                    @Valid @RequestBody ArticleRequest articleRequest){
        articleService.updateArticleById(id,articleRequest.getTitle(),articleRequest.getContent());
        return "修改成功";
    }
}
