package org.example.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseService  {
    SseEmitter subscribe(Long userId);
    void sendEvent(Long userId, String eventName, Object data);
}
