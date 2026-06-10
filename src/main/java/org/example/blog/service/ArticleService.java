package org.example.blog.service;

import org.example.blog.entity.Article;

import java.util.List;

public interface ArticleService {
    //创建文章
    void createArticle(Long authorId,String title,String content);

    List<Article> getAllArticle(); //无条件,相当于 Select * from article

    //根据 id 查找文章
    Article getArticleById(Long id);
}
