package org.example.blog.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.example.blog.dto.request.PageRequest;
import org.example.blog.dto.response.ArticleResponse;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.entity.Article;
import org.example.blog.entity.ArticleCollect;
import org.example.blog.mapper.ArticleCollectMapper;
import org.example.blog.mapper.ArticleMapper;
import org.example.blog.service.CollectService;
import org.example.blog.service.NotificationService;
import org.example.blog.util.UserContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollectServiceImpl extends ServiceImpl<ArticleCollectMapper, ArticleCollect> implements CollectService {
    private final NotificationService notificationService;

    private final ArticleMapper articleMapper;

    private final ArticleQueryService articleQueryService;

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
            this.save(articleCollect);
        }
    }

    @Override
    public Long getCollectCount(Long articleId) {
        return this.count(new LambdaQueryWrapper<ArticleCollect>().eq(ArticleCollect::getArticleId, articleId));
    }

    @Override
    public boolean isCollect(Long articleId, Long userId) {
        LambdaQueryWrapper<ArticleCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleCollect::getArticleId, articleId).eq(ArticleCollect::getUserId, userId);
        return this.count(wrapper) > 0;
    }

    @Override
    public Set<Long> getMyCollect(List<Long> articleIds, Long userId) {
        return this.list(new LambdaQueryWrapper<ArticleCollect>().eq(ArticleCollect::getUserId, userId).in(ArticleCollect::getArticleId, articleIds))
                .stream().map(ArticleCollect::getArticleId).collect(Collectors.toSet());
    }

    @Override
    public PageResponse<ArticleResponse> pageMyCollect(PageRequest request) {
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
        Page<Article> p = new Page<>(request.getPage(), request.getSize());
        articleMapper.selectPage(p, wrapper);
        List<ArticleResponse> list = BeanUtil.copyToList(p.getRecords(), ArticleResponse.class);

        articleQueryService.enrich(list, articleIds, UserContext.get());

        response.setTotal(p.getTotal());
        response.setList(list);
        return response;
    }
}
