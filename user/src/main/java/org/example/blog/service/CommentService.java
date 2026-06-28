package org.example.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.blog.dto.request.CommentRequest;
import org.example.blog.dto.request.CommentSearchRequest;
import org.example.blog.dto.request.PageRequest;
import org.example.blog.dto.response.CommentResponse;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.entity.Comment;

import java.util.List;

public interface CommentService extends IService<Comment> {

    void createCommentByArticleId(CommentRequest request);

    Comment getCommentById(Long id);

    void deleteCommentById(Long id);

    void updateCommentById(CommentRequest request);

    PageResponse<CommentResponse> pageComment(CommentSearchRequest request);

    void adminDeleteCommentById(Long id);
}
