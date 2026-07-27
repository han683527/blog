package org.example.blog.service;

import org.example.blog.dto.request.ArticleSearchRequest;
import org.example.blog.dto.response.ArticleResponse;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.entity.Article;

public interface ArticleSearchService{
    // 同步一篇文章到 ES (新增/修改是调用)
    void syncArticle(Article article);

    // 从 ES 中删除一篇文章
    void deleteArticle(Long id);

    // ES 搜索(关键词分词+高亮)
    PageResponse<ArticleResponse> search(ArticleSearchRequest request);
}
