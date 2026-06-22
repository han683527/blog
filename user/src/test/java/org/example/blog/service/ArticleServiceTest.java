package org.example.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.blog.dto.response.ArticleResponse;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.entity.Article;
import org.example.blog.exception.ForbiddenException;
import org.example.blog.exception.NotFoundException;
import org.example.blog.util.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ArticleServiceTest {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private CategoryService categoryService;

    private Long categoryId;

    @BeforeEach
    void setUp() {
        UserContext.set(1L);
        categoryService.createCategory("测试分类");
        LambdaQueryWrapper<org.example.blog.entity.Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(org.example.blog.entity.Category::getCategoryName, "测试分类");
        categoryId = categoryService.getOne(wrapper).getId();
    }

    @AfterEach
    void tearDown() {
        UserContext.remove();
    }

    // ---------- 创建 ----------

    @Test
    void testCreateArticle_Success() {
        articleService.createArticle(1L, "测试标题", "测试内容", categoryId);

        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getTitle, "测试标题");
        Article article = articleService.getOne(wrapper);
        assertNotNull(article);
        assertEquals("测试内容", article.getContent());
        assertEquals(1L, article.getAuthorId());
        assertEquals(categoryId, article.getCategoryId());
    }

    // ---------- 根据 ID 查询 ----------

    @Test
    void testGetArticleById_Success() {
        articleService.createArticle(1L, "测试标题", "测试内容", categoryId);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getTitle, "测试标题");
        Long id = articleService.getOne(wrapper).getId();

        Article article = articleService.getArticleById(id);
        assertNotNull(article);
        assertEquals("测试标题", article.getTitle());
    }

    @Test
    void testGetArticleById_NotFound() {
        assertThrows(NotFoundException.class,
                () -> articleService.getArticleById(999L));
    }

    // ---------- 删除 ----------

    @Test
    void testDeleteArticleById_Success() {
        articleService.createArticle(1L, "测试标题", "测试内容", categoryId);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getTitle, "测试标题");
        Long id = articleService.getOne(wrapper).getId();

        articleService.deleteArticleById(id);
        assertThrows(NotFoundException.class,
                () -> articleService.getArticleById(id));
    }

    @Test
    void testDeleteArticleById_NotFound() {
        assertThrows(NotFoundException.class,
                () -> articleService.deleteArticleById(999L));
    }

    @Test
    void testDeleteArticleById_Forbidden() {
        articleService.createArticle(1L, "测试标题", "测试内容", categoryId);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getTitle, "测试标题");
        Long id = articleService.getOne(wrapper).getId();

        UserContext.set(2L);
        assertThrows(ForbiddenException.class,
                () -> articleService.deleteArticleById(id));
    }

    // ---------- 更新 ----------

    @Test
    void testUpdateArticleById_Success() {
        articleService.createArticle(1L, "测试标题", "测试内容", categoryId);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getTitle, "测试标题");
        Long id = articleService.getOne(wrapper).getId();

        articleService.updateArticleById(id, "新标题", "新内容", null);
        Article article = articleService.getArticleById(id);
        assertEquals("新标题", article.getTitle());
        assertEquals("新内容", article.getContent());
    }

    @Test
    void testUpdateArticleById_NotFound() {
        assertThrows(NotFoundException.class,
                () -> articleService.updateArticleById(999L, "标题", "内容", null));
    }

    @Test
    void testUpdateArticleById_Forbidden() {
        articleService.createArticle(1L, "测试标题", "测试内容", categoryId);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getTitle, "测试标题");
        Long id = articleService.getOne(wrapper).getId();

        UserContext.set(2L);
        assertThrows(ForbiddenException.class,
                () -> articleService.updateArticleById(id, "新标题", "新内容", null));
    }

    // ---------- 分页 ----------

    @Test
    void testPageArticle() {
        articleService.createArticle(1L, "文章1", "内容1", categoryId);
        articleService.createArticle(1L, "文章2", "内容2", categoryId);

        PageResponse<ArticleResponse> result = articleService.pageArticle(1, 10, null);
        assertTrue(result.getTotal() >= 2);
        assertFalse(result.getList().isEmpty());
    }

    @Test
    void testPageArticle_ByCategory() {
        articleService.createArticle(1L, "分类文章1", "内容", categoryId);
        articleService.createArticle(1L, "分类文章2", "内容", categoryId);

        PageResponse<ArticleResponse> result = articleService.pageArticle(1, 10, categoryId);
        assertEquals(2, result.getTotal());
    }

    // ---------- 搜索 ----------

    @Test
    void testSearchArticle() {
        articleService.createArticle(1L, "Spring Boot入门", "内容", categoryId);
        articleService.createArticle(1L, "Redis实战", "内容", categoryId);

        PageResponse<ArticleResponse> result = articleService.searchArticleByTitleKeyword("Spring", 1, 10);
        assertEquals(1, result.getTotal());
    }

    @Test
    void testSearchArticle_EmptyResult() {
        PageResponse<ArticleResponse> result = articleService.searchArticleByTitleKeyword("不存在的关键字", 1, 10);
        assertEquals(0, result.getTotal());
        assertTrue(result.getList().isEmpty());
    }
}
