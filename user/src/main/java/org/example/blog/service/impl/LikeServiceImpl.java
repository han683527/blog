package org.example.blog.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.blog.dto.request.ArticleSearchRequest;
import org.example.blog.dto.response.ArticleResponse;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.entity.Article;
import org.example.blog.entity.ArticleLike;
import org.example.blog.mapper.ArticleLikeMapper;
import org.example.blog.service.ArticleService;
import org.example.blog.service.LikeService;
import org.example.blog.service.NotificationService;
import org.example.blog.util.UserContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LikeServiceImpl extends ServiceImpl<ArticleLikeMapper, ArticleLike> implements LikeService{

    private final NotificationService notificationService;
    private final ArticleService articleService;
    private final ArticleQueryService articleQueryService;

    public LikeServiceImpl(NotificationService notificationService,
                           @Lazy ArticleService articleService,
                           @Lazy ArticleQueryService articleQueryService) {
        this.notificationService = notificationService;
        this.articleService = articleService;
        this.articleQueryService = articleQueryService;
    }

    @Override
    public void toggle(Long articleId){
        Long userId = UserContext.get();
        LambdaQueryWrapper<ArticleLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleLike::getArticleId,articleId).eq(ArticleLike::getUserId,userId);
        // 点赞 -> 未点赞
        if(this.count(wrapper)>0){
            this.remove(wrapper);
        } else {
            ArticleLike articleLike = new ArticleLike();
            articleLike.setArticleId(articleId);
            articleLike.setUserId(userId);
            notificationService.createNotification(userId,articleId,"LIKE");
            this.save(articleLike);
        }
    }

    @Override
    public PageResponse<ArticleResponse> pageMyLike(ArticleSearchRequest request) {
        Long userId = UserContext.get();
        // 找当前用户的点赞并按创建时间排序
        List<ArticleLike> likes = this.list(new LambdaQueryWrapper<ArticleLike>().eq(ArticleLike::getUserId,userId).orderByDesc(ArticleLike::getCreateTime));
        List<Long> articleIds = likes.stream().map(ArticleLike::getArticleId).collect(Collectors.toList());

        if (articleIds.isEmpty()) {
            PageResponse<ArticleResponse> response = new PageResponse<>();
            response.setTotal(0L);
            response.setList(List.of());
            return response;
        }

        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Article::getId, articleIds).orderByDesc(Article::getCreateTime);
        Page<Article> p = articleService.page(new Page<>(request.getPage(), request.getSize()),wrapper);
        List<ArticleResponse> list = BeanUtil.copyToList(p.getRecords(), ArticleResponse.class);

        articleQueryService.enrich(list, articleIds, UserContext.get());

        PageResponse<ArticleResponse> response = new PageResponse<>();
        response.setTotal(p.getTotal());
        response.setList(list);
        return response;
    }
}
