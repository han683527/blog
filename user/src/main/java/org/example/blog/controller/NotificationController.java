package org.example.blog.controller;

import lombok.RequiredArgsConstructor;
import org.example.blog.dto.request.PageRequest;
import org.example.blog.dto.response.NotificationResponse;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.dto.response.Result;
import org.example.blog.service.NotificationService;
import org.example.blog.service.SseService;
import org.example.blog.util.JwtUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RequiredArgsConstructor
@RestController
@RequestMapping("/notification")
public class NotificationController {

    private final NotificationService notificationService;
    private final SseService sseService;
    private final JwtUtil jwtUtil;

    @PostMapping("/list")
    public Result<PageResponse<NotificationResponse>> pageNotification(@RequestBody PageRequest request) {
        return Result.success(notificationService.pageNotifications(request));
    }

    @PostMapping("/read/{id}")
    public Result<String> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return Result.success("已读");
    }

    @PostMapping("/read/all")
    public Result<String> markAsReadAll() {
        notificationService.markAllAsRead();
        return Result.success("全部已读");
    }

    @GetMapping("/unread")
    public Result<Long> getUnreadCount(){
        return Result.success(notificationService.getUnreadCount());
    }

    @GetMapping("/subscribe")
    public SseEmitter subscribe(@RequestParam String token) {
        // 不能用 UserContext.get() ,因为 subscribe 已经被排除在外
        Long userId = jwtUtil.validateAndGetUserId(token);
        return sseService.subscribe(userId);
    }
}
