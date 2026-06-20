package org.example.blog.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.blog.dto.request.ArticleRequest;
import org.example.blog.dto.response.ArticleResponse;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.dto.response.Result;
import org.example.blog.entity.Article;
import org.example.blog.service.ArticleService;
import org.example.blog.util.UserContext;
import org.springframework.web.bind.annotation.*;


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

//    // 相比与一次查询所有的数据,可以用分页查询显示数据,不用一次性全部返回参数(这个过程要时间渲染)
//    @GetMapping
//    public Result<Article> getAllArticle(){
//        return Result.success(articleService.getAllArticle());
//    }
    @GetMapping
    public Result<PageResponse<ArticleResponse>> getAllArticle(@RequestParam(defaultValue = "1") int page,
                                                               @RequestParam(defaultValue = "10") int size){
        return Result.success(articleService.pageArticle(page,size));
    }

    @GetMapping("/{id}")
    public Result<Article> getArticleById(@PathVariable Long id){
        return Result.success(articleService.getArticleById(id));
    }

    @GetMapping("/search/{keyword}")
    public Result<PageResponse<ArticleResponse>> searchArticle(@PathVariable String keyword,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size){
        return Result.success(articleService.searchArticle(keyword,page,size));
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