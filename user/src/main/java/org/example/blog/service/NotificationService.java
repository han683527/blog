package org.example.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.blog.dto.request.PageRequest;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.entity.Notification;
import org.example.blog.mapper.NotificationMapper;

public interface NotificationService extends IService<Notification>  {
    void createNotification(Long actorId, Long articleId, String type);

    PageResponse<Notification> pageNotifications(PageRequest pageRequest);

    // 标记单个已读
    void markAsRead(Long id);

    // 一键已读
    void markAllAsRead();

    Long getUnreadCount();
}
