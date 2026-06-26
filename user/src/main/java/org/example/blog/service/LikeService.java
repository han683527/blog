package org.example.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.blog.dto.request.PageRequest;
import org.example.blog.dto.response.ArticleResponse;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.entity.ArticleLike;

import java.util.List;
import java.util.Set;

public interface LikeService extends IService<ArticleLike> {
    void toggle(Long articleId);

    Long getLikeCount(Long articleId);

    boolean isLiked(Long articleId, Long userId);

    Set<Long> getMyLike(List<Long> articleId, Long userId);

    PageResponse<ArticleResponse> pageMyLike(PageRequest request);
}
