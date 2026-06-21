package org.example.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.blog.dto.response.ArticleResponse;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.entity.Article;

import java.util.List;

public interface ArticleService extends IService<Article> {

    // 创建文章
    void createArticle(Long authorId,String title,String content);

    // 根据 id 查找文章
    Article getArticleById(Long id);

    // 根据 id 删除文章
    void deleteArticleById(Long id);

    // 修改文章
    void updateArticleById(Long id,String title,String content);

    // 分页
    PageResponse<ArticleResponse> pageArticle(int page, int size);

    //按标题进行模糊查找
    PageResponse<ArticleResponse> searchArticle(String keyword,int page,int size);
}
