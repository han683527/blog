package org.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.blog.dto.response.ArticleResponse;
import org.example.blog.entity.*;
import org.example.blog.mapper.ArticleCollectMapper;
import org.example.blog.mapper.ArticleLikeMapper;
import org.example.blog.mapper.ArticleTagMapper;
import org.example.blog.mapper.CommentMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ArticleQueryService {

    private final ArticleTagMapper articleTagMapper;
    private final CommentMapper commentMapper;
    private final ArticleLikeMapper articleLikeMapper;
    private final ArticleCollectMapper articleCollectMapper;

    public void enrich(List<ArticleResponse> list, List<Long> articleIds, Long userId) {
        if (articleIds == null || articleIds.isEmpty()) return;

        // 标签
        List<ArticleTag> allTags = articleTagMapper.selectList(
                new LambdaQueryWrapper<ArticleTag>().in(ArticleTag::getArticleId, articleIds));
        Map<Long, List<Long>> tagMap = allTags.stream()
                .collect(Collectors.groupingBy(ArticleTag::getArticleId,
                        Collectors.mapping(ArticleTag::getTagId, Collectors.toList())));

        // 点赞数
        Map<Long, Long> likeCountMap = articleLikeMapper.selectList(
                        new LambdaQueryWrapper<ArticleLike>().in(ArticleLike::getArticleId, articleIds))
                .stream().collect(Collectors.groupingBy(ArticleLike::getArticleId, Collectors.counting()));
        // 收藏数
        Map<Long, Long> collectCountMap = articleCollectMapper.selectList(
                        new LambdaQueryWrapper<ArticleCollect>().in(ArticleCollect::getArticleId, articleIds))
                .stream().collect(Collectors.groupingBy(ArticleCollect::getArticleId, Collectors.counting()));
        // 评论数
        Map<Long, Long> commentCountMap = commentMapper.selectList(
                        new LambdaQueryWrapper<Comment>().in(Comment::getArticleId, articleIds))
                .stream().collect(Collectors.groupingBy(Comment::getArticleId, Collectors.counting()));

        // 当前用户点赞/收藏状态
        Set<Long> likedIds = articleLikeMapper.selectList(
                        new LambdaQueryWrapper<ArticleLike>()
                                .eq(ArticleLike::getUserId, userId)
                                .in(ArticleLike::getArticleId, articleIds))
                .stream().map(ArticleLike::getArticleId).collect(Collectors.toSet());
        Set<Long> collectedIds = articleCollectMapper.selectList(
                        new LambdaQueryWrapper<ArticleCollect>()
                                .eq(ArticleCollect::getUserId, userId)
                                .in(ArticleCollect::getArticleId, articleIds))
                .stream().map(ArticleCollect::getArticleId).collect(Collectors.toSet());

        for (ArticleResponse response : list) {
            response.setTags(tagMap.getOrDefault(response.getId(), List.of()));
            response.setLikeCount(likeCountMap.getOrDefault(response.getId(), 0L));
            response.setIsLike(likedIds.contains(response.getId()));
            response.setCollectCount(collectCountMap.getOrDefault(response.getId(), 0L));
            response.setIsCollect(collectedIds.contains(response.getId()));
            response.setCommentCount(commentCountMap.getOrDefault(response.getId(), 0L));
        }
    }
}
