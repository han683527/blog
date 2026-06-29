package org.example.blog.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.blog.dto.request.PageRequest;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.entity.Article;
import org.example.blog.entity.Notification;
import org.example.blog.mapper.NotificationMapper;
import org.example.blog.service.ArticleService;
import org.example.blog.service.NotificationService;
import org.example.blog.util.UserContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {

    private final ArticleService articleService;

    public NotificationServiceImpl(@Lazy ArticleService articleService) {
        this.articleService = articleService;
    }

    @Override
    public void createNotification(Long actorId, Long articleId, String type) {
        Article article = articleService.getById(articleId);
        if(article.getAuthorId().equals(actorId)){ // 自己操作自己的不进行通知
            return;
        }

        Notification notification = new Notification();
        notification.setUserId(article.getAuthorId());
        notification.setActorId(actorId);
        notification.setArticleId(articleId);
        notification.setType(type);
        this.save(notification);
    }

    @Override
    public PageResponse<Notification> pageNotifications(PageRequest pageRequest) {
        Long userId = UserContext.get();
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId,userId);
        Page<Notification> p = this.page(new Page<>(pageRequest.getPage(), pageRequest.getSize()),wrapper);
        List<Notification> list = BeanUtil.copyToList(p.getRecords(), Notification.class);
        PageResponse<Notification> response = new PageResponse<>();
        response.setTotal(p.getTotal());
        response.setList(list);
        return response;
    }

    @Override
    public void markAsRead(Long id) {
        Notification notification = new Notification();
        notification.setId(id);
        notification.setReadFlag(true);
        this.updateById(notification);
    }

    @Override
    public void markAllAsRead() {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, UserContext.get())
                .eq(Notification::isReadFlag, false);
        Notification notification = new Notification();
        notification.setReadFlag(true);
        this.update(notification,wrapper);
    }

    public Long getUnreadCount() {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId,UserContext.get())
                .eq(Notification::isReadFlag, false);
        return this.count(wrapper);
    }
}
