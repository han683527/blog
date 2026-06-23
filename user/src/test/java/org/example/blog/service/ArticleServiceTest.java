package org.example.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.blog.dto.request.ArticleRequest;
import org.example.blog.dto.request.ArticleSearchRequest;
import org.example.blog.dto.request.CategoryRequest;
import org.example.blog.dto.response.ArticleResponse;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.entity.Article;
import org.example.blog.entity.Category;
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

        CategoryRequest catReq = new CategoryRequest();
        catReq.setCategoryName("测试分类");
        categoryService.createCategory(catReq);

        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getCategoryName, "测试分类");
        categoryId = categoryService.getOne(wrapper).getId();
    }

    @AfterEach
    void tearDown() {
        UserContext.remove();
    }

    private ArticleRequest createArticleRequest(String title, String content, Long categoryId) {
        ArticleRequest req = new ArticleRequest();
        req.setTitle(title);
        req.setContent(content);
        req.setCategoryId(categoryId);
        return req;
    }

    // ---------- 创建 ----------

    @Test
    void testCreateArticle_Success() {
        articleService.createArticle(createArticleRequest("测试标题", "测试内容", categoryId));

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
        articleService.createArticle(createArticleRequest("测试标题", "测试内容", categoryId));
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
        articleService.createArticle(createArticleRequest("测试标题", "测试内容", categoryId));
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
        articleService.createArticle(createArticleRequest("测试标题", "测试内容", categoryId));
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
        articleService.createArticle(createArticleRequest("测试标题", "测试内容", categoryId));
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getTitle, "测试标题");
        Long id = articleService.getOne(wrapper).getId();

        ArticleRequest updateReq = new ArticleRequest();
        updateReq.setId(id);
        updateReq.setTitle("新标题");
        updateReq.setContent("新内容");
        articleService.updateArticleById(updateReq);

        Article article = articleService.getArticleById(id);
        assertEquals("新标题", article.getTitle());
        assertEquals("新内容", article.getContent());
    }

    @Test
    void testUpdateArticleById_NotFound() {
        ArticleRequest req = new ArticleRequest();
        req.setId(999L);
        req.setTitle("标题");
        req.setContent("内容");
        assertThrows(NotFoundException.class,
                () -> articleService.updateArticleById(req));
    }

    @Test
    void testUpdateArticleById_Forbidden() {
        articleService.createArticle(createArticleRequest("测试标题", "测试内容", categoryId));
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getTitle, "测试标题");
        Long id = articleService.getOne(wrapper).getId();

        UserContext.set(2L);
        ArticleRequest req = new ArticleRequest();
        req.setId(id);
        req.setTitle("新标题");
        req.setContent("新内容");
        assertThrows(ForbiddenException.class,
                () -> articleService.updateArticleById(req));
    }

    // ---------- 分页 ----------

    @Test
    void testPageArticle() {
        articleService.createArticle(createArticleRequest("文章1", "内容1", categoryId));
        articleService.createArticle(createArticleRequest("文章2", "内容2", categoryId));

        ArticleSearchRequest searchReq = new ArticleSearchRequest();
        searchReq.setPage(1);
        searchReq.setSize(10);
        PageResponse<ArticleResponse> result = articleService.pageArticle(searchReq);
        assertTrue(result.getTotal() >= 2);
        assertFalse(result.getList().isEmpty());
    }

    @Test
    void testPageArticle_ByCategory() {
        articleService.createArticle(createArticleRequest("分类文章1", "内容", categoryId));
        articleService.createArticle(createArticleRequest("分类文章2", "内容", categoryId));

        ArticleSearchRequest searchReq = new ArticleSearchRequest();
        searchReq.setPage(1);
        searchReq.setSize(10);
        searchReq.setCategoryId(categoryId);
        PageResponse<ArticleResponse> result = articleService.pageArticle(searchReq);
        assertEquals(2, result.getTotal());
    }

    // ---------- 搜索 ----------

    @Test
    void testSearchArticle() {
        articleService.createArticle(createArticleRequest("Spring Boot入门", "内容", categoryId));
        articleService.createArticle(createArticleRequest("Redis实战", "内容", categoryId));

        ArticleSearchRequest searchReq = new ArticleSearchRequest();
        searchReq.setKeyword("Spring");
        searchReq.setPage(1);
        searchReq.setSize(10);
        PageResponse<ArticleResponse> result = articleService.pageArticle(searchReq);
        assertEquals(1, result.getTotal());
    }

    @Test
    void testSearchArticle_EmptyResult() {
        ArticleSearchRequest searchReq = new ArticleSearchRequest();
        searchReq.setKeyword("不存在的关键字");
        searchReq.setPage(1);
        searchReq.setSize(10);
        PageResponse<ArticleResponse> result = articleService.pageArticle(searchReq);
        assertEquals(0, result.getTotal());
        assertTrue(result.getList().isEmpty());
    }
}
