package org.example.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.blog.entity.Comment;

import java.util.List;

public interface CommentService  {

    void createComment(Long userId,Long articleId,String content);

    List<Comment> getCommentByArticleId(Long articleId);


}
