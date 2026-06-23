package org.example.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.blog.dto.request.CategoryRequest;
import org.example.blog.dto.request.ArticleRequest;
import org.example.blog.dto.request.CommentRequest;
import org.example.blog.dto.request.CommentSearchRequest;
import org.example.blog.dto.response.CommentResponse;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.entity.Article;
import org.example.blog.entity.Category;
import org.example.blog.entity.Comment;
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
public class CommentServiceTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private CategoryService categoryService;

    private Long articleId;

    @BeforeEach
    void setUp() {
        UserContext.set(1L);

        CategoryRequest catReq = new CategoryRequest();
        catReq.setCategoryName("测试分类");
        categoryService.createCategory(catReq);

        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getCategoryName, "测试分类");
        Long categoryId = categoryService.getOne(wrapper).getId();

        ArticleRequest articleReq = new ArticleRequest();
        articleReq.setTitle("测试文章");
        articleReq.setContent("测试内容");
        articleReq.setCategoryId(categoryId);
        articleService.createArticle(articleReq);

        LambdaQueryWrapper<Article> aWrapper = new LambdaQueryWrapper<>();
        aWrapper.eq(Article::getTitle, "测试文章");
        articleId = articleService.getOne(aWrapper).getId();
    }

    @AfterEach
    void tearDown() {
        UserContext.remove();
    }

    private CommentRequest createCommentRequest(String content, Long articleId) {
        CommentRequest req = new CommentRequest();
        req.setContent(content);
        req.setArticleId(articleId);
        return req;
    }

    // ---------- 创建 ----------

    @Test
    void testCreateComment_Success() {
        commentService.createCommentByArticleId(createCommentRequest("测试评论", articleId));
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getContent, "测试评论");
        Comment comment = commentService.getOne(wrapper);
        assertNotNull(comment);
        assertEquals(articleId, comment.getArticleId());
    }

    @Test
    void testCreateComment_ArticleNotFound() {
        CommentRequest req = createCommentRequest("评论", 999L);
        assertThrows(NotFoundException.class,
                () -> commentService.createCommentByArticleId(req));
    }

    // ---------- 根据 ID 查询 ----------

    @Test
    void testGetCommentById_Success() {
        commentService.createCommentByArticleId(createCommentRequest("测试评论", articleId));
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getContent, "测试评论");
        Long id = commentService.getOne(wrapper).getId();

        Comment comment = commentService.getCommentById(id);
        assertNotNull(comment);
        assertEquals("测试评论", comment.getContent());
    }

    @Test
    void testGetCommentById_NotFound() {
        assertThrows(NotFoundException.class,
                () -> commentService.getCommentById(999L));
    }

    // ---------- 删除 ----------

    @Test
    void testDeleteCommentById_Success() {
        commentService.createCommentByArticleId(createCommentRequest("测试评论", articleId));
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getContent, "测试评论");
        Long id = commentService.getOne(wrapper).getId();

        commentService.deleteCommentById(id);
        assertThrows(NotFoundException.class,
                () -> commentService.getCommentById(id));
    }

    @Test
    void testDeleteCommentById_NotFound() {
        assertThrows(NotFoundException.class,
                () -> commentService.deleteCommentById(999L));
    }

    @Test
    void testDeleteCommentById_Forbidden() {
        commentService.createCommentByArticleId(createCommentRequest("测试评论", articleId));
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getContent, "测试评论");
        Long id = commentService.getOne(wrapper).getId();

        UserContext.set(2L);
        assertThrows(ForbiddenException.class,
                () -> commentService.deleteCommentById(id));
    }

    // ---------- 更新 ----------

    @Test
    void testUpdateCommentById_Success() {
        commentService.createCommentByArticleId(createCommentRequest("测试评论", articleId));
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getContent, "测试评论");
        Long id = commentService.getOne(wrapper).getId();

        CommentRequest updateReq = new CommentRequest();
        updateReq.setId(id);
        updateReq.setContent("更新后的评论");
        commentService.updateCommentById(updateReq);

        Comment comment = commentService.getCommentById(id);
        assertEquals("更新后的评论", comment.getContent());
    }

    @Test
    void testUpdateCommentById_NotFound() {
        CommentRequest req = new CommentRequest();
        req.setId(999L);
        req.setContent("新内容");
        assertThrows(NotFoundException.class,
                () -> commentService.updateCommentById(req));
    }

    @Test
    void testUpdateCommentById_Forbidden() {
        commentService.createCommentByArticleId(createCommentRequest("测试评论", articleId));
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getContent, "测试评论");
        Long id = commentService.getOne(wrapper).getId();

        UserContext.set(2L);
        CommentRequest req = new CommentRequest();
        req.setId(id);
        req.setContent("新内容");
        assertThrows(ForbiddenException.class,
                () -> commentService.updateCommentById(req));
    }

    // ---------- 按文章分页查询 ----------

    @Test
    void testPageCommentByArticleId() {
        commentService.createCommentByArticleId(createCommentRequest("评论1", articleId));
        commentService.createCommentByArticleId(createCommentRequest("评论2", articleId));

        CommentSearchRequest searchReq = new CommentSearchRequest();
        searchReq.setArticleId(articleId);
        searchReq.setPage(1);
        searchReq.setSize(10);
        PageResponse<CommentResponse> result = commentService.pageComment(searchReq);
        assertEquals(2, result.getTotal());
    }

    @Test
    void testPageCommentByArticleId_ArticleNotFound() {
        CommentSearchRequest searchReq = new CommentSearchRequest();
        searchReq.setArticleId(999L);
        searchReq.setPage(1);
        searchReq.setSize(10);
        PageResponse<CommentResponse> result = commentService.pageComment(searchReq);
        assertEquals(0, result.getTotal());
    }
}
