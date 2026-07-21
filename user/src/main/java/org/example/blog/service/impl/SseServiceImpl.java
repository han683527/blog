package org.example.blog.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.blog.service.SseService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE 连接管理器
 * <p>
 * 核心逻辑:
 * - 每个登录用户对应一个 SseEmitter 长连接(存在 Map 里)
 * - subscribe(): 用户登录后建立连接(订阅)
 * - sendEvent(): 有通知/新评论等事件时推送给指定用户
 */
@Slf4j
@Service
public class SseServiceImpl implements SseService {

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    @Override
    public SseEmitter subscribe(Long userId) {
        // 如果用户已有旧连接,先关闭(防止同一个用户多个连接)
        SseEmitter old = emitters.remove(userId);
        if (old != null) {
            old.complete();
        }

        // 创建新连接,设置超时时间 30 分钟,30 分钟后连接自动断开,前端 EventSource 会自动重连
        SseEmitter emitter = new SseEmitter(1800000L);
        emitters.put(userId, emitter);

        // 连接正常关闭时,从 Map 中移除
        emitter.onCompletion(() -> {
            emitters.remove(userId);
            log.info("SSE 连接关闭: userId={}", userId);
        });

        // 连接超时时,从 Map 中移除
        emitter.onTimeout(() -> {
            emitters.remove(userId);
            log.info("SSE 连接超时: userId={}", userId);
        });

        // 连接出错是, 从 Map 中移除
        emitter.onError(e -> {
            emitters.remove(userId);
            log.info("SSE 连接错误: userId={}", userId);
        });

        log.info("SSE 连接建立成功: userId={}", userId);
        return emitter;
    }

    public void sendEvent(Long userId, String eventName, Object data) {
        // 先从 Map 中找到该用户的 SSE 连接
        SseEmitter emitter = emitters.get(userId);

        if (emitter != null) {
            try {
                // 推送事件给前端
                // eventName 是事件名称,前端用 addEventListener(eventName, callback) 监听
                emitter.send(SseEmitter.event().name(eventName).data(data));
                log.info("SSE 推送成功: userId={}, event={}", userId, eventName);
            } catch (IOException e) {
                //推送失败(比如用户已经断开连接), 移除该连接
                emitters.remove(userId);
                log.warn("SSE 推送失败, 已移除连接: userId={}", userId);
            }
        }
    }
}
