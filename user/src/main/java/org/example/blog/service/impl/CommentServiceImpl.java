package org.example.blog.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.blog.dto.response.CommentResponse;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.entity.Comment;
import org.example.blog.exception.ForbiddenException;
import org.example.blog.exception.NotFoundException;
import org.example.blog.mapper.ArticleMapper;
import org.example.blog.mapper.CommentMapper;
import org.example.blog.service.CommentService;
import org.example.blog.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper,Comment> implements CommentService {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public void createComment(Long userId,Long articleId,String content) {
        if(articleMapper.selectById(articleId) == null){
            throw new NotFoundException("文章不存在");
        }
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setArticleId(articleId);
        comment.setUserId(userId);
        this.save(comment);
    }

    public Comment getCommentById(Long id){
        String key = "comment:" + id;
        String cached = redisTemplate.opsForValue().get(key);
        if(cached != null){
            log.info("缓存命中: {}",key);
            if("NULL".equals(cached)){
                throw new NotFoundException("评论不存在");
            }
            return JSONUtil.toBean(cached,Comment.class);
        }

        Comment comment = this.getById(id);
        if(comment == null){
            redisTemplate.opsForValue().set(key,"NULL",1, TimeUnit.MINUTES);
            log.info("写入空值缓存(防穿透): {}",key);
            throw new NotFoundException("评论不存在");
        }

        redisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(comment),10, TimeUnit.MINUTES);
        log.info("写入缓存: {}",key);
        return comment;
    }

    @Override
    public void deleteCommentById(Long id) {
        Comment comment = this.getById(id);
        if(comment == null){
            throw new NotFoundException("评论不存在");
        }
        if(comment.getUserId() != UserContext.get()){
            throw new ForbiddenException("不能删除别人的评论");
        }
        this.removeById(id);

        redisTemplate.delete("comment:" + id);
        log.info("删除缓存: comment: {}",comment);
    }

    @Override
    public void updateCommentById(String content,Long id) {
        Comment comment = this.getById(id);
        if(comment == null){
            throw new NotFoundException("评论不存在");
        }
        if(comment.getUserId() != UserContext.get()){
            throw new ForbiddenException("不能修改别人的评论");
        }
        comment.setContent(content);
        this.updateById(comment);

        redisTemplate.delete("comment:" + id);
        log.info("删除缓存: comment: {}",comment);
    }

    @Override
    public PageResponse<CommentResponse> pageComment(int page, int size) {
        Page<Comment> p = this.page(new Page<>(page,size));
        List<CommentResponse> list = BeanUtil.copyToList(p.getRecords(),CommentResponse.class);
        PageResponse<CommentResponse> response = new PageResponse<>();
        response.setTotal(p.getTotal());
        response.setList(list);
        return response;
    }

    @Override
    public PageResponse<CommentResponse> pageCommentByArticleId(Long articleId, int page, int size) {
        if(articleMapper.selectById(articleId) == null){
            throw new NotFoundException("文章不存在");
        }
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getArticleId,articleId);
        Page<Comment> p = this.page(new Page<>(page,size),wrapper);
        List<CommentResponse> list = BeanUtil.copyToList(p.getRecords(),CommentResponse.class);
        PageResponse<CommentResponse> response = new PageResponse<CommentResponse>();
        response.setTotal(p.getTotal());
        response.setList(list);
        return response;
    }
}
