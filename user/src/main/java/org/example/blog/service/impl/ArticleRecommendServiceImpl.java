package org.example.blog.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.blog.dto.response.ArticleResponse;
import org.example.blog.entity.*;
import org.example.blog.service.*;
import org.example.blog.util.UserContext;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ArticleRecommendServiceImpl implements ArticleRecommendService {

    private final CommentService commentService;
    private final LikeService likeService;
    private final CollectService collectService;
    private final ViewHistoryService viewHistoryService;
    private final ArticleService articleService;
    private final ArticleQueryService articleQueryService;
    private final ArticleTagService articleTagService;

    public ArticleRecommendServiceImpl(CommentService commentService, LikeService likeService, CollectService collectService, ViewHistoryService viewHistoryService, ArticleService articleService, ArticleQueryService articleQueryService, ArticleTagService articleTagService) {
        this.commentService = commentService;
        this.likeService = likeService;
        this.collectService = collectService;
        this.viewHistoryService = viewHistoryService;
        this.articleService = articleService;
        this.articleQueryService = articleQueryService;
        this.articleTagService = articleTagService;
    }

    @Override
    public List<ArticleResponse> recommend(int size) {
        Long userId = UserContext.get();

        // 情况一: 游客/新用户(无任何行为动作记录) -> 推荐热门文章
        if (userId == null || hasNoBehavior(userId)) {
            return hotArticles(size);
        }
        // 情况二: 用户有行为记录 -> 内容个性化推荐
        return personalized(userId, size);
    }

    private boolean hasNoBehavior(Long userId) {
        // 查评论、点赞、收藏、以及浏览记录
        // 该坑点: LambdaQueryWrapper<Comment> comment = new LambdaQueryWrapper<Comment>().eq(Comment::getUserId, userId);
        // 这段代码的结果一定不为 null 导致 if 语句无论如何都判断不出来
        // 改成 count 记录存在条数
        boolean hasComment = commentService.count(new LambdaQueryWrapper<Comment>().eq(Comment::getUserId, userId)) > 0;
        boolean hasLike = likeService.count(new LambdaQueryWrapper<ArticleLike>().eq(ArticleLike::getUserId, userId)) > 0;
        boolean hasCollect = collectService.count(new LambdaQueryWrapper<ArticleCollect>().eq(ArticleCollect::getUserId, userId)) > 0;
        boolean hasViewHistory = viewHistoryService.count(new LambdaQueryWrapper<ViewHistory>().eq(ViewHistory::getUserId, userId)) > 0;

        if (!hasComment && !hasLike && !hasCollect && !hasViewHistory) {
            return true;
        }
        return false;
    }

    private List<ArticleResponse> hotArticles(int size) {
        // 1.查文章
        List<Article> articles = articleService.list(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getStatus, 1)
                        .orderByDesc(Article::getViewCount)
                        .last("limit 50"));

        // 2.转为 response 补全信息
        List<ArticleResponse> list = BeanUtil.copyToList(articles, ArticleResponse.class);
        List<Long> articleIds = articles.stream().map(Article::getId).collect(Collectors.toList());
        articleQueryService.enrich(list, articleIds, UserContext.get());

        // 3.根据公式计算热度进行排序
        list.sort(Comparator.comparingLong(this::hotScore).reversed());
        return list.size() > size ? list.subList(0, size) : list;
    }

    private List<ArticleResponse> personalized(Long userId, int size) {
        // 1.收集行为权重
        Map<Long, Integer> actionWeight = new HashMap<>();
        // 1.1 浏览
        List<ViewHistory> viewHistories = viewHistoryService.list(
                new LambdaQueryWrapper<ViewHistory>()
                        .eq(ViewHistory::getUserId, userId)
                        .orderByDesc(ViewHistory::getViewTime)
                        .last("limit 30")
        );

        for (ViewHistory viewHistory : viewHistories) {
            actionWeight.merge(viewHistory.getArticleId(), 1, Integer::sum);
        }

        // 1.2 点赞
        List<ArticleLike> likes = likeService.list(
                new LambdaQueryWrapper<ArticleLike>()
                        .eq(ArticleLike::getUserId, userId)
                        .orderByDesc(ArticleLike::getCreateTime)
                        .last("limit 30")
        );

        for (ArticleLike like : likes) {
            actionWeight.merge(like.getArticleId(), 2, Integer::sum);
        }

        // 1.3 收藏
        List<ArticleCollect> collects = collectService.list(
                new LambdaQueryWrapper<ArticleCollect>()
                        .eq(ArticleCollect::getUserId, userId)
                        .orderByDesc(ArticleCollect::getCreateTime)
                        .last("limit 30")
        );

        for (ArticleCollect collect : collects) {
            actionWeight.merge(collect.getArticleId(), 4, Integer::sum);
        }

        // 1.4 评论
        List<Comment> comments = commentService.list(
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getUserId, userId)
                        .orderByDesc(Comment::getCreateTime)
                        .last("limit 30")
        );

        for (Comment comment : comments) {
            actionWeight.merge(comment.getArticleId(), 2, Integer::sum);
        }

        // 2.由文章行为反查偏好分类和标签
        // 2.1 查出用户互动过的已发布文章
        List<Article> activeArticles = articleService.list(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getStatus, 1)
                        .in(Article::getId, actionWeight.keySet())
        );

        // 2.2 转移权重到分类和标签
        Map<Long, Integer> categoryWeight = new HashMap<>();
        for (Article article : activeArticles) {
            categoryWeight.merge(article.getCategoryId(), actionWeight.get(article.getId()), Integer::sum);
        }

        // 处理和文章相关的每个 Tag
        List<ArticleTag> articleTags = articleTagService.list(
                new LambdaQueryWrapper<ArticleTag>()
                        .in(ArticleTag::getArticleId, actionWeight.keySet())
        );
        Map<Long, Integer> tagWeight = new HashMap<>();
        for (ArticleTag articleTag : articleTags) {
            tagWeight.merge(articleTag.getTagId(), actionWeight.get(articleTag.getArticleId()), Integer::sum);
        }

        // 3.取偏好 top 分类、top 标签
        List<Long> topCategoryIds = categoryWeight.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed()) // 按分数排序
                .limit(3)
                .map(Map.Entry::getKey)                                         // 保留 id
                .collect(Collectors.toList());

        List<Long> topTagIds = tagWeight.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(6)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 4.构造文章候选集合
        // 4.1 命中偏好分类的文章
        List<Article> candidateArticles = new ArrayList<>();
        if (!topCategoryIds.isEmpty()) {
            candidateArticles = articleService.list(
                    new LambdaQueryWrapper<Article>()
                            .eq(Article::getStatus, 1)                  // 排除未发布的
                            .in(Article::getCategoryId, topCategoryIds)     // 只要偏好分类的
                            .notIn(Article::getId, actionWeight.keySet())  // 排除已经交互的
                            .ne(Article::getAuthorId, userId)               // 排除自己的
            );
        }

        // 4.2 命中偏好标签的文章
        if (!topTagIds.isEmpty()) {
            List<Long> articleIds = articleTagService.list(
                            new LambdaQueryWrapper<ArticleTag>()
                                    .in(ArticleTag::getTagId, topTagIds)
                    ).stream()
                    .map(ArticleTag::getArticleId)
                    .distinct()
                    .collect(Collectors.toList());
            if (!articleIds.isEmpty()) {
                candidateArticles.addAll(articleService.list(
                        new LambdaQueryWrapper<Article>()
                                .eq(Article::getStatus, 1)                  // 排除未发布的
                                .in(Article::getId, articleIds)                 // 保留有偏好标签的
                                .notIn(Article::getId, actionWeight.keySet())  // 排除已经交互的
                                .ne(Article::getAuthorId, userId)               // 排除自己的
                ));
            }
        }

        // 4.3 去除重复文章
        Map<Long, Article> candidateArticlesMap = new HashMap<>();
        for (Article article : candidateArticles) {
            candidateArticlesMap.put(article.getId(), article);
        }
        List<Article> topArticles = new ArrayList<>(candidateArticlesMap.values());

        // 5.打分排序
        // 5.1 查候选文章的各自标签
        Map<Long, List<Long>> articleTagMap = new HashMap<>();
        List<Long> topArticleIds = topArticles.stream().map(Article::getId).collect(Collectors.toList());
        if (!topArticles.isEmpty()) {
            articleTagService.list(
                            new LambdaQueryWrapper<ArticleTag>()
                                    .in(ArticleTag::getArticleId, topArticleIds))
                    // .forEach() 遍历每一行,把这行的 tagId 塞到对应 articleId 的列表里
                    // computeIfAbsent(key, 工厂函数):
                    //      - 如果 map 里已经有这个 key  → 直接返回已有的列表(不执行工厂函数)
                    //      - 如果 map 里还没有这个 key  → 执行工厂函数 new 一个空列表,放进 map,返回它
                    .forEach(articleTag -> articleTagMap.computeIfAbsent(articleTag.getArticleId(),
                            k -> new ArrayList<>()).add(articleTag.getTagId()));
        }

        // 5.2 打分
        Map<Long, Long> scoreMap = new HashMap<>();
        for (Article article : topArticles) {
            long score = 0;
            // 命中偏好分类,权重 * 2
            score += categoryWeight.getOrDefault(article.getCategoryId(), 0) * 2;
            // 命中偏好标签,累加
            for (Long tagId : articleTagMap.getOrDefault(article.getId(), Collections.emptyList())) {
                score += tagWeight.getOrDefault(tagId, 0);
            }
            scoreMap.put(article.getId(), score);
        }

        // 5.3 排序
        topArticles.sort(Comparator
                // 给定一篇文章 a , 从 scoreMap 取出它的分数
                .comparingLong((Article a) -> scoreMap.get(a.getId()))
                .thenComparingLong(Article::getViewCount)
                .reversed());

        // 5.4 候选为空,返回热门
        if (topArticles.isEmpty()) {
            return hotArticles(size);
        }

        // 5.5 转 response 补全信息
        List<ArticleResponse> list = BeanUtil.copyToList(topArticles, ArticleResponse.class);
        articleQueryService.enrich(list, topArticleIds, UserContext.get());

        return list.size() > size ? list.subList(0, size) : list;
    }

    private long hotScore(ArticleResponse response) {
        return response.getViewCount() + response.getLikeCount() * 2
                + response.getCollectCount() * 3 + response.getCommentCount() * 2;
    }
}
