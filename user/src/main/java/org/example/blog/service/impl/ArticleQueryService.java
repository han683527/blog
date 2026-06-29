package org.example.blog.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.blog.dto.response.ArticleResponse;
import org.example.blog.entity.*;
import org.example.blog.service.*;
import org.example.blog.util.UserContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ArticleQueryService {

    private final StringRedisTemplate redisTemplate;
    private final ArticleTagService articleTagService;
    private final LikeService likeService;
    private final CollectService collectService;
    private final ArticleService articleService;
    private final CommentService commentService;

    public void enrich(List<ArticleResponse> list, List<Long> articleIds, Long userId) {
        if (articleIds == null || articleIds.isEmpty()) return;

        // 标签
        List<ArticleTag> allTags = articleTagService.list(
                new LambdaQueryWrapper<ArticleTag>().in(ArticleTag::getArticleId, articleIds));
        Map<Long, List<Long>> tagMap = allTags.stream()
                .collect(Collectors.groupingBy(ArticleTag::getArticleId,
                        Collectors.mapping(ArticleTag::getTagId, Collectors.toList())));

        // 点赞数
        Map<Long, Long> likeCountMap = likeService.list(
                        new LambdaQueryWrapper<ArticleLike>().in(ArticleLike::getArticleId, articleIds))
                .stream().collect(Collectors.groupingBy(ArticleLike::getArticleId, Collectors.counting()));
        // 收藏数
        Map<Long, Long> collectCountMap = collectService.list(
                        new LambdaQueryWrapper<ArticleCollect>().in(ArticleCollect::getArticleId, articleIds))
                .stream().collect(Collectors.groupingBy(ArticleCollect::getArticleId, Collectors.counting()));
        // 评论数
        Map<Long, Long> commentCountMap = commentService.list(
                        new LambdaQueryWrapper<Comment>().in(Comment::getArticleId, articleIds))
                .stream().collect(Collectors.groupingBy(Comment::getArticleId, Collectors.counting()));

        // 当前用户点赞/收藏状态
        Set<Long> likedIds = likeService.list(
                        new LambdaQueryWrapper<ArticleLike>()
                                .eq(ArticleLike::getUserId, userId)
                                .in(ArticleLike::getArticleId, articleIds))
                .stream().map(ArticleLike::getArticleId).collect(Collectors.toSet());
        Set<Long> collectedIds = collectService.list(
                        new LambdaQueryWrapper<ArticleCollect>()
                                .eq(ArticleCollect::getUserId, userId)
                                .in(ArticleCollect::getArticleId, articleIds))
                .stream().map(ArticleCollect::getArticleId).collect(Collectors.toSet());

        // 返回 标签(多个) | 点赞数 | 点赞状态(是否点赞) | 收藏数 | 收藏状态 | 评论数
        for (ArticleResponse response : list) {
            response.setTags(tagMap.getOrDefault(response.getId(), List.of()));
            response.setLikeCount(likeCountMap.getOrDefault(response.getId(), 0L));
            response.setIsLike(likedIds.contains(response.getId()));
            response.setCollectCount(collectCountMap.getOrDefault(response.getId(), 0L));
            response.setIsCollect(collectedIds.contains(response.getId()));
            response.setCommentCount(commentCountMap.getOrDefault(response.getId(), 0L));
        }
    }

    public ArticleResponse buildArticleResponse(Article article, String key) {
        article.setViewCount(article.getViewCount() == null ? 0L : article.getViewCount() + 1);
        articleService.updateById(article);
        redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(article), 10, TimeUnit.MINUTES);
        ArticleResponse response = BeanUtil.toBean(article, ArticleResponse.class);
        enrich(List.of(response), List.of(article.getId()), UserContext.get());
        return response;
    }
}
