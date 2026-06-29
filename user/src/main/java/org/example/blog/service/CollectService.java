package org.example.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.blog.dto.request.ArticleSearchRequest;
import org.example.blog.dto.response.ArticleResponse;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.entity.ArticleCollect;

public interface CollectService extends IService<ArticleCollect> {
    void toggle(Long articleId);

    PageResponse<ArticleResponse> pageMyCollect(ArticleSearchRequest request);
}
