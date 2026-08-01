package org.example.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.blog.entity.ViewHistory;

public interface ViewHistoryService extends IService<ViewHistory> {
    void record(Long userId, Long articleId);
}
