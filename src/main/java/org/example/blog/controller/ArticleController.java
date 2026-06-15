package org.example.blog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.blog.dto.request.ArticleRequest;
import org.example.blog.dto.response.Result;
import org.example.blog.entity.Article;
import org.example.blog.service.ArticleService;
import org.example.blog.util.UserContext;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/article")
@RequiredArgsConstructor // 采用 @RequiredArgsConstructor 构造器的注解简化了注入
public class ArticleController {

    private final ArticleService articleService;

    @PostMapping
    public Result<String> createArticle(@Valid @RequestBody ArticleRequest articleRequest){
        Long userId = UserContext.get();
        articleService.createArticle(userId,articleRequest.getTitle(),articleRequest.getContent());
        return Result.success("发布成功");
    }

    @GetMapping
    public Result<IPage<Article>> getAllArticle(@RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size){
        return Result.success(articleService.pageArticle(page,size));
    }

    @GetMapping("/{id}")
    public Result<Article> getArticleById(@PathVariable Long id){
        return Result.success(articleService.getArticleById(id));
    }

//    @DeleteMapping
//    public String deleteAllArticle(){
//        articleService.deleteAllArticle();
//        return "删除成功";
//    }

    @DeleteMapping("/{id}")
    public Result<String> deleteArticleById(@PathVariable Long id){
        articleService.deleteArticleById(id);
        return Result.success("删除成功");
    }

    @PutMapping("/{id}")
    public Result<String> updateArticleById(@PathVariable Long id ,
                                    @Valid @RequestBody ArticleRequest articleRequest){
        articleService.updateArticleById(id,articleRequest.getTitle(),articleRequest.getContent());
        return Result.success("修改成功");
    }
}
