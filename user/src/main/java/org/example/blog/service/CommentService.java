package org.example.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.blog.dto.response.CommentResponse;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.entity.Comment;

import java.util.List;

public interface CommentService extends IService<Comment> {

    void createComment(Long userId,Long articleId,String content);

    Comment getCommentById(Long id);

    void deleteCommentById(Long id);

    void updateCommentById(String content,Long id);

    PageResponse<CommentResponse> pageComment(int page, int size);

    PageResponse<CommentResponse> pageCommentByArticleId(Long articleId, int page, int size);
}
