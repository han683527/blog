package org.example.blog.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.blog.dto.request.CommentRequest;
import org.example.blog.dto.request.CommentSearchRequest;
import org.example.blog.dto.response.CommentResponse;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.entity.Article;
import org.example.blog.entity.Comment;
import org.example.blog.entity.Notification;
import org.example.blog.entity.User;
import org.example.blog.exception.BadRequestException;
import org.example.blog.exception.ForbiddenException;
import org.example.blog.exception.NotFoundException;
import org.example.blog.mapper.CommentMapper;
import org.example.blog.service.*;
import org.example.blog.util.UserContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final StringRedisTemplate redisTemplate;
    private final NotificationService notificationService;
    private final UserService userService;
    private final ArticleService articleService;
    private final SseService sseService;

    public CommentServiceImpl(StringRedisTemplate redisTemplate,
                              NotificationService notificationService,
                              UserService userService,
                              SseService sseService,
                              @Lazy ArticleService articleService) {
        this.redisTemplate = redisTemplate;
        this.notificationService = notificationService;
        this.userService = userService;
        this.sseService = sseService;
        this.articleService = articleService;
    }

    @Override
    public void createCommentByArticleId(CommentRequest request) {
        Long articleId = request.getArticleId();
        if (request.getId() != null) {
            throw new BadRequestException("创建评论时不能指定 ID");
        }

        if (articleService.getById(articleId) == null) {
            throw new NotFoundException("文章不存在");
        }
        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setArticleId(articleId);
        comment.setUserId(UserContext.get());
        this.save(comment);
        notificationService.createNotification(UserContext.get(), articleId, "COMMENT");

        // 推送新评论给文章作者
        Article article = articleService.getById(articleId);
        Map<String, Object> data = new HashMap<>();
        data.put("type", "NEW_COMMENT");
        data.put("articleId", articleId);

        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setArticleId(articleId);
        response.setUserId(comment.getUserId());

        User user = userService.getById(comment.getUserId());
        if (user != null) {
            response.setUserName(user.getNickname());
            response.setUserAvatar(user.getAvatar());
        }
        data.put("comment", response);
        sseService.sendEvent(article.getAuthorId(), "comment", data);
    }

    public Comment getCommentById(Long id) {
        String key = "comment:" + id;
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            log.info("缓存命中: {}", key);
            if ("NULL".equals(cached)) {
                throw new NotFoundException("评论不存在");
            }
            return JSONUtil.toBean(cached, Comment.class);
        }

        Comment comment = this.getById(id);
        if (comment == null) {
            redisTemplate.opsForValue().set(key, "NULL", 1, TimeUnit.MINUTES);
            log.info("写入空值缓存(防穿透): {}", key);
            throw new NotFoundException("评论不存在");
        }

        redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(comment), 10, TimeUnit.MINUTES);
        log.info("写入缓存: {}", key);
        return comment;
    }

    @Override
    public void deleteCommentById(Long id) {
        Comment comment = this.getById(id);
        if (comment == null) {
            throw new NotFoundException("评论不存在");
        }

        // 对于一篇文章下的评论:只有文章作者和评论者可以删除
        // 评论者自己可以删除评论
        if (!comment.getUserId().equals(UserContext.get())) {
            Article article = articleService.getById(comment.getArticleId());
            // 文章作者可以删除文章下的评论 -> 给评论者发通知
            if (article == null || !article.getAuthorId().equals(UserContext.get())) {
                throw new ForbiddenException("不能删除别人的评论");
            }
            Notification notification = new Notification();
            notification.setUserId(comment.getUserId()); // 接收者是被删评的人
            notification.setActorId(UserContext.get()); // 发送者是当前登录用户(即作者)
            notification.setArticleId(comment.getArticleId());
            notification.setType("DELETE_COMMENT");
            notificationService.save(notification);

            Map<String, Object> data = new HashMap<>();
            data.put("type", "DELETE_COMMENT");
            data.put("articleId", comment.getArticleId());
            sseService.sendEvent(comment.getUserId(), "comment", data);
        }
        this.removeById(id);

        redisTemplate.delete("comment:" + id);
        log.info("删除缓存: comment: {}", comment);
    }

    // 查询文章/用户自己的评论
    @Override
    public PageResponse<CommentResponse> pageComment(CommentSearchRequest request) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();

        // 查找文章下的评论
        Long articleId = request.getArticleId();
        if (articleId != null) {
            wrapper.eq(Comment::getArticleId, articleId);
        }

        // 按 userId 查找用户的评论
        Long userId = request.getUserId();
        if (userId != null) {
            wrapper.eq(Comment::getUserId, userId);
        }

        Page<Comment> p = this.page(new Page<>(request.getPage(), request.getSize()), wrapper);
        List<CommentResponse> list = BeanUtil.copyToList(p.getRecords(), CommentResponse.class);
        for (CommentResponse commentResponse : list) {
            User user = userService.getById(commentResponse.getUserId());
            if (user != null) {
                commentResponse.setUserName(user.getNickname());
                commentResponse.setUserAvatar(user.getAvatar());
            }
        }
        PageResponse<CommentResponse> response = new PageResponse<>();
        response.setTotal(p.getTotal());
        response.setList(list);
        return response;
    }

    @Override
    public void adminDeleteCommentById(Long id) {
        userService.checkAdmin();
        Comment comment = this.getOptById(id)
                .orElseThrow(() -> new NotFoundException("评论不存在"));

        // 管理员可以对所有评论进行删除,不用关注评论的归属
        this.removeById(comment.getId());
        redisTemplate.delete("comment:" + id);
        log.info("管理员删除评论: {}", id);
    }

    @Override
    public PageResponse<CommentResponse> adminPageComment(CommentSearchRequest request) {
        userService.checkAdmin();
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        Page<Comment> p = this.page(new Page<>(request.getPage(), request.getSize()), wrapper);
        List<CommentResponse> list = BeanUtil.copyToList(p.getRecords(), CommentResponse.class);
        PageResponse<CommentResponse> response = new PageResponse<>();
        response.setTotal(p.getTotal());
        response.setList(list);
        return response;
    }
}
