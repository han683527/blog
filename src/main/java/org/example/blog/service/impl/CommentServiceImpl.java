package org.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.blog.entity.Comment;
import org.example.blog.mapper.ArticleMapper;
import org.example.blog.mapper.CommentMapper;
import org.example.blog.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Override
    public void createComment(Long userId,Long articleId,String content) {
        if(articleMapper.selectById(articleId) == null){
            throw new RuntimeException("文章不存在");
        }
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setArticleId(articleId);
        comment.setUserId(userId);
        commentMapper.insert(comment);
    }

    //按照文章查评论
    @Override
    public List<Comment> getCommentByArticleId(Long articleId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<Comment>();
        wrapper.eq(Comment::getArticleId,articleId);
        return commentMapper.selectList(wrapper);
    }
}
