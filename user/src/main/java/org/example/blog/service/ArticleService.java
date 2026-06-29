package org.example.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.blog.dto.request.ArticleRequest;
import org.example.blog.dto.request.ArticleSearchRequest;
import org.example.blog.dto.request.PageRequest;
import org.example.blog.dto.response.ArticleResponse;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.entity.Article;

import java.util.List;

public interface ArticleService extends IService<Article> {

    // 创建文章
    void createArticle(ArticleRequest articleRequest);

    // 根据(分类、标签)查询文章
    PageResponse<ArticleResponse> pageArticle(ArticleSearchRequest request);

    // 根据 id 查找文章
    ArticleResponse getArticleById(Long id);

    // 根据 id 删除文章
    void deleteArticleById(Long id);

    // 修改文章
    void updateArticleById(ArticleRequest request);

    // 后台管理删除
    void adminDeleteArticleById(Long id);

    // 后台管理分页查询
    PageResponse<ArticleResponse> adminPageArticle(ArticleSearchRequest request);

    // 后台管理修改
    void adminUpdateArticle(ArticleRequest request);
}
