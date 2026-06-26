package org.example.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.blog.dto.request.PageRequest;
import org.example.blog.dto.response.ArticleResponse;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.entity.ArticleCollect;

import java.util.List;
import java.util.Set;

public interface CollectService extends IService<ArticleCollect> {
    void toggle(Long articleId);

    Long getCollectCount(Long articleId);

    boolean isCollect(Long articleId, Long userId);

    Set<Long> getMyCollect(List<Long> articleIds, Long userId);

    PageResponse<ArticleResponse> pageMyCollect(PageRequest request);
}
