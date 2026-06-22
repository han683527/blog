package org.example.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.blog.dto.response.CommentResponse;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.entity.Article;
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
        categoryService.createCategory("测试分类");
        LambdaQueryWrapper<org.example.blog.entity.Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(org.example.blog.entity.Category::getCategoryName, "测试分类");
        Long categoryId = categoryService.getOne(wrapper).getId();

        articleService.createArticle(1L, "测试文章", "测试内容", categoryId);
        LambdaQueryWrapper<Article> aWrapper = new LambdaQueryWrapper<>();
        aWrapper.eq(Article::getTitle, "测试文章");
        articleId = articleService.getOne(aWrapper).getId();
    }

    @AfterEach
    void tearDown() {
        UserContext.remove();
    }

    // ---------- 创建 ----------

    @Test
    void testCreateComment_Success() {
        commentService.createComment(1L, articleId, "测试评论");
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getContent, "测试评论");
        Comment comment = commentService.getOne(wrapper);
        assertNotNull(comment);
        assertEquals(articleId, comment.getArticleId());
    }

    @Test
    void testCreateComment_ArticleNotFound() {
        assertThrows(NotFoundException.class,
                () -> commentService.createComment(1L, 999L, "评论"));
    }

    // ---------- 根据 ID 查询 ----------

    @Test
    void testGetCommentById_Success() {
        commentService.createComment(1L, articleId, "测试评论");
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
        commentService.createComment(1L, articleId, "测试评论");
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
        commentService.createComment(1L, articleId, "测试评论");
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
        commentService.createComment(1L, articleId, "测试评论");
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getContent, "测试评论");
        Long id = commentService.getOne(wrapper).getId();

        commentService.updateCommentById( "更新后的评论",id);
        Comment comment = commentService.getCommentById(id);
        assertEquals("更新后的评论", comment.getContent());
    }

    @Test
    void testUpdateCommentById_NotFound() {
        assertThrows(NotFoundException.class,
                () -> commentService.updateCommentById( "新内容",999L));
    }

    @Test
    void testUpdateCommentById_Forbidden() {
        commentService.createComment(1L, articleId, "测试评论");
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getContent, "测试评论");
        Long id = commentService.getOne(wrapper).getId();

        UserContext.set(2L);
        assertThrows(ForbiddenException.class,
                () -> commentService.updateCommentById("新内容",id));
    }

    // ---------- 按文章分页查询 ----------

    @Test
    void testPageCommentByArticleId() {
        commentService.createComment(1L, articleId, "评论1");
        commentService.createComment(1L, articleId, "评论2");

        PageResponse<CommentResponse> result = commentService.pageCommentByArticleId(articleId, 1, 10);
        assertEquals(2, result.getTotal());
    }

    @Test
    void testPageCommentByArticleId_ArticleNotFound() {
        assertThrows(NotFoundException.class,
                () -> commentService.pageCommentByArticleId(999L, 1, 10));
    }
}
