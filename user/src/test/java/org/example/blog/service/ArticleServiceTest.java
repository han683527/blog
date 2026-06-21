package org.example.blog.service;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.blog.entity.Article;
import org.example.blog.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional // 测试完自动回滚,不影响数据库
public class ArticleServiceTest {

    @Autowired
    private ArticleService articleService;

    @Test
    // NotFoundException
    void GetArticleById1(){
        assertThrows(NotFoundException.class,
                () -> articleService.getArticleById(99L));
    }

    @Test
    void GetArticleById2(){
        articleService.createArticle(1L,"测试标题","测试内容");

        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getTitle,"测试标题");
        Article id = articleService.getOne(wrapper);
        Article article = articleService.getArticleById(id.getId());
        assertNotNull(article);
        assertEquals("测试标题",article.getTitle());
    }
}
