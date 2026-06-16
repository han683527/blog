package org.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.blog.entity.Comment;
import org.example.blog.mapper.ArticleMapper;
import org.example.blog.mapper.CommentMapper;
import org.example.blog.service.CommentService;
import org.example.blog.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper,Comment> implements CommentService {

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
        this.save(comment);
    }

    //按照 id 查找评论
    @Override
    public Comment getCommentById(Long id){
        Comment comment = this.getById(id);
        if(comment == null){
            throw new RuntimeException("评论不存在");
        }
        return comment;
    }

    //按照文章查所有评论
    @Override
    public List<Comment> getAllComment() {
        return this.list();
    }

    @Override
    public void deleteCommentById(Long id) {
        Comment comment = this.getById(id);
        if(comment == null){
            throw new RuntimeException("评论不存在");
        }
        if(comment.getUserId() != UserContext.get()){
            throw new RuntimeException("不能删除别人的评论");
        }
        this.removeById(id);
    }

    @Override
    public void updateCommentById(Long id, String content) {
        Comment comment = this.getById(id);
        if(comment == null){
            throw new RuntimeException("评论不存在");
        }
        if(comment.getUserId() != UserContext.get()){
            throw new RuntimeException("不能修改别人的评论");
        }
        comment.setContent(content);
        this.updateById(comment);
    }

    @Override
    public IPage<Comment> pageComment(int page,int size) {
        return this.page(new Page<>(page,size));
    }
}
