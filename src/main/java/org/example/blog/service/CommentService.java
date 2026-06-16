package org.example.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.blog.entity.Comment;

import java.util.List;

public interface CommentService extends IService<Comment> {

    void createComment(Long userId,Long articleId,String content);

    Comment getCommentById(Long id);

    List<Comment> getAllComment();

    void deleteCommentById(Long id);

    void updateCommentById(Long id,String content);

    IPage<Comment> pageComment(int page,int size);
}
