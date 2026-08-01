package org.example.blog.service;

import org.example.blog.dto.response.ArticleResponse;

import java.util.List;

public interface ArticleRecommendService {
    List<ArticleResponse> recommend(int size);
}
