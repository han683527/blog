package org.example.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.blog.dto.request.ArticleSearchRequest;
import org.example.blog.dto.response.ArticleResponse;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.entity.ArticleLike;

public interface LikeService extends IService<ArticleLike> {
    void toggle(Long articleId);

    PageResponse<ArticleResponse> pageMyLike(ArticleSearchRequest request);
}
