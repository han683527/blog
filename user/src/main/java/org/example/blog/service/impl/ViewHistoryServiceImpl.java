package org.example.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.blog.entity.ViewHistory;
import org.example.blog.mapper.ViewHistoryMapper;
import org.example.blog.service.ViewHistoryService;
import org.springframework.stereotype.Service;

@Service
public class ViewHistoryServiceImpl extends ServiceImpl<ViewHistoryMapper, ViewHistory> implements ViewHistoryService {

    @Override
    public void record(Long userId, Long articleId) {
        ViewHistory viewHistory = new ViewHistory();
        viewHistory.setUserId(userId);
        viewHistory.setArticleId(articleId);
        this.save(viewHistory);
    }
}
