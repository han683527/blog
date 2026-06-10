package org.example.blog.service.impl;

import org.example.blog.entity.Article;
import org.example.blog.mapper.ArticleMapper;
import org.example.blog.service.ArticleService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;

    public ArticleServiceImpl(ArticleMapper articleMapper){
        this.articleMapper = articleMapper;
    }

    @Override
    public void createArticle(Long authorId, String title, String content) {
        Article article = new Article();
        article.setAuthorId(authorId);
        article.setTitle(title);
        article.setContent(content);
        articleMapper.insert(article);
    }

    @Override
    public List<Article> getAllArticle() {
        return articleMapper.selectList(null); //这个作用是什么
    }

    @Override
    public Article getArticleById(Long id) {
        Article article = articleMapper.selectById(id);
        if(article == null){
            throw new RuntimeException("文章不存在");
        }
        return article;
    }
}
