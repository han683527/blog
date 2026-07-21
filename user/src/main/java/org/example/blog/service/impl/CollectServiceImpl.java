package org.example.blog.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.blog.dto.request.ArticleSearchRequest;
import org.example.blog.dto.response.ArticleResponse;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.entity.Article;
import org.example.blog.entity.ArticleCollect;
import org.example.blog.mapper.ArticleCollectMapper;
import org.example.blog.service.ArticleService;
import org.example.blog.service.CollectService;
import org.example.blog.service.NotificationService;
import org.example.blog.service.SseService;
import org.example.blog.util.UserContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CollectServiceImpl extends ServiceImpl<ArticleCollectMapper, ArticleCollect> implements CollectService {
    private final NotificationService notificationService;
    private final ArticleService articleService;
    private final ArticleQueryService articleQueryService;
    private final SseService sseService;

    public CollectServiceImpl(NotificationService notificationService,
                              @Lazy ArticleService articleService,
                              @Lazy ArticleQueryService articleQueryService, SseService sseService) {
        this.notificationService = notificationService;
        this.articleService = articleService;
        this.articleQueryService = articleQueryService;
        this.sseService = sseService;
    }

    @Override
    public void toggle(Long articleId) {
        Long userId = UserContext.get();
        LambdaQueryWrapper<ArticleCollect> wrapper = new LambdaQueryWrapper<ArticleCollect>();
        wrapper.eq(ArticleCollect::getArticleId, articleId).eq(ArticleCollect::getUserId, userId);
        if (this.count(wrapper) > 0) {
            this.remove(wrapper);
        } else {
            ArticleCollect articleCollect = new ArticleCollect();
            articleCollect.setArticleId(articleId);
            articleCollect.setUserId(userId);
            notificationService.createNotification(userId, articleId, "COLLECT");

            // 推送收藏信息
            Article article = articleService.getById(articleId);
            Map<String, Object> data = new HashMap<>();
            data.put("type", "NEW_COLLECT");
            data.put("articleId", articleId);
            sseService.sendEvent(article.getAuthorId(), "notification", data);
            this.save(articleCollect);
        }
    }

    @Override
    public PageResponse<ArticleResponse> pageMyCollect(ArticleSearchRequest request) {
        Long userId = UserContext.get();
        List<ArticleCollect> collects = this.list(
                new LambdaQueryWrapper<ArticleCollect>()
                        .eq(ArticleCollect::getUserId, userId)
                        .orderByDesc(ArticleCollect::getCreateTime));
        List<Long> articleIds = collects.stream()
                .map(ArticleCollect::getArticleId).collect(Collectors.toList());

        PageResponse<ArticleResponse> response = new PageResponse<>();
        if (articleIds.isEmpty()) {
            response.setTotal(0L);
            response.setList(List.of());
            return response;
        }

        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Article::getId, articleIds).orderByDesc(Article::getCreateTime);
        Page<Article> p = articleService.page(new Page<>(request.getPage(), request.getSize()),wrapper);
        List<ArticleResponse> list = BeanUtil.copyToList(p.getRecords(), ArticleResponse.class);

        articleQueryService.enrich(list, articleIds, UserContext.get());

        response.setTotal(p.getTotal());
        response.setList(list);
        return response;
    }
}
