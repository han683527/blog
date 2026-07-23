package org.example.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.blog.dto.request.CommentRequest;
import org.example.blog.dto.request.CommentSearchRequest;
import org.example.blog.dto.response.CommentResponse;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.entity.Comment;

public interface CommentService extends IService<Comment> {

    // 在某篇文章下创建评论
    void createCommentByArticleId(CommentRequest request);

    // 获取评论
    Comment getCommentById(Long id);

    // 删除评论
    void deleteCommentById(Long id);

    // 分页查询评论
    PageResponse<CommentResponse> pageComment(CommentSearchRequest request);

    // 后台管理删除评论
    void adminDeleteCommentById(Long id);

    // 后台管理分页查询评论
    PageResponse<CommentResponse> adminPageComment(CommentSearchRequest request);
}
