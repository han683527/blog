package org.example.blog.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.blog.dto.request.ArticleRequest;
import org.example.blog.dto.request.ArticleSearchRequest;
import org.example.blog.dto.response.ArticleResponse;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.entity.*;
import org.example.blog.exception.BadRequestException;
import org.example.blog.exception.ForbiddenException;
import org.example.blog.exception.NotFoundException;
import org.example.blog.mapper.ArticleMapper;
import org.example.blog.service.*;
import org.example.blog.util.UserContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    private final StringRedisTemplate redisTemplate;
    private final CommentService commentService;
    private final ArticleQueryService articleQueryService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final TagService tagService;
    private final ArticleTagService articleTagService;
    private final ArticleSearchService articleSearchService;

    public ArticleServiceImpl(StringRedisTemplate redisTemplate,
                              @Lazy CommentService commentService,
                              ArticleTagService articleTagService,
                              CategoryService categoryService,
                              ArticleQueryService articleQueryService,
                              UserService userService,
                              TagService tagService,
                              ArticleSearchService articleSearchService) {
        this.redisTemplate = redisTemplate;
        this.commentService = commentService;
        this.articleQueryService = articleQueryService;
        this.userService = userService;
        this.categoryService = categoryService;
        this.articleTagService = articleTagService;
        this.tagService = tagService;
        this.articleSearchService = articleSearchService;
    }

    @Override
    public void createArticle(ArticleRequest request) {
        String title = request.getTitle();
        Long userId = UserContext.get();
        Article article = new Article();
        if (request.getId() != null) {
            throw new BadRequestException("创建文章时不能指定 ID");
        }

        article.setAuthorId(userId);
        article.setTitle(title);
        article.setContent(request.getContent());
        article.setStatus(request.getStatus());

        // 判断分类是否存在
        if (request.getCategoryId() != null) {
            Category category = categoryService.getById(request.getCategoryId());
            if (category == null) {
                throw new NotFoundException("分类不存在");
            }
            article.setCategoryId(request.getCategoryId());
        }

        this.save(article);

        // 先有数据才能插标签,否则会读到 null
        List<Long> tagIds = request.getTagIds();

        // 判断标签是否存在
        if (tagIds != null && !tagIds.isEmpty()) {
            List<Tag> tags = tagService.listByIds(tagIds);
            if (tags.size() != tagIds.size()) {
                throw new NotFoundException("部分标签不存在");
            }
            for (Long tagId : tagIds) {
                ArticleTag articleTag = new ArticleTag();
                articleTag.setArticleId(article.getId());
                articleTag.setTagId(tagId);
                articleTagService.save(articleTag);
            }
        }

        // 根据发布状态决定是否存入 ES 库
        if (request.getStatus() == 1) {
            articleSearchService.syncArticle(article);
        }
        log.info("用户 {} 创建文章 {}", userId, title);
    }

    @Override
    public PageResponse<ArticleResponse> pageArticle(ArticleSearchRequest request) {
        // 判断是否有关键词
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            // 有关键词 -> 走 ES 搜索
            return articleSearchService.search(request);
        }


        // 没有关键词 -> 按原有逻辑继续
        // 1.什么都不做 ; 2.模糊查找 ; 3.种类 ; 4.标签
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();

        // 模糊查找(搜索已用 ES 代替)
        String keyword = request.getKeyword();
        if (keyword != null && !keyword.isEmpty()) {
            log.info("关键词: {}", keyword);
            wrapper.like(Article::getTitle, keyword);
        }

        // 种类
        Long categoryId = request.getCategoryId();
        if (categoryId != null) {
            wrapper.eq(Article::getCategoryId, categoryId);
        }

        // 查找具有该标签的文章
        List<Long> tagIds = request.getTagIds();
        if (tagIds != null && !tagIds.isEmpty()) {
            List<ArticleTag> articleTags = articleTagService.list(new LambdaQueryWrapper<ArticleTag>().in(ArticleTag::getTagId, tagIds));
            List<Long> articleIds = articleTags.stream().map(ArticleTag::getArticleId).distinct().collect(Collectors.toList());
            wrapper.in(Article::getId, articleIds);
        }

        // 按 authorId 查文章
        Long authorId = request.getAuthorId();
        if (authorId != null) {
            wrapper.eq(Article::getAuthorId, authorId);
        }

        // 只显示已发布的文章
        if (authorId == null || !authorId.equals(UserContext.get())) {
            wrapper.eq(Article::getStatus, 1);
        }

        wrapper.orderByDesc(Article::getViewCount);

        // 如果上述条件均不触发就只执行分页查找
        int page = request.getPage();
        int size = request.getSize();
        Page<Article> p = this.page(new Page<>(page, size), wrapper);
        // Hutool 的根据方法:内部调用反射遍历 Article 的 getter,找到 ArticleResponse 里同名的字段,复制过去
        List<ArticleResponse> list = BeanUtil.copyToList(p.getRecords(), ArticleResponse.class);

        // 查找一篇文章具有的所有标签,点赞数,收藏数
        List<Long> articleIdList = p.getRecords().stream()
                .map(Article::getId).
                collect(Collectors.toList());
        articleQueryService.enrich(list, articleIdList, UserContext.get());

        PageResponse<ArticleResponse> response = new PageResponse<>();
        response.setTotal(p.getTotal());
        response.setList(list);
        return response;
    }

    public ArticleResponse getArticleById(Long id) {
        // 1.先查缓存
        String key = "article:" + id; // 定义一个存取的钥匙
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            log.info("缓存命中: {}", key);
            if ("NULL".equals(cached)) {
                throw new NotFoundException("文章不存在");
            }
            // 缓存命中后应将缓存 JSON 反序列化为 Article
            Article article = JSONUtil.toBean(cached, Article.class);

            return articleQueryService.buildArticleResponse(article, key);
        }

        // 2.缓存没有,查数据库
        Article article = this.getById(id);
        if (article == null) {
            redisTemplate.opsForValue().set(key, "NULL", 1, TimeUnit.MINUTES);
            log.info("写入空值缓存(防穿透): {}", key);
            throw new NotFoundException("文章不存在");
        }

        // 草稿检查
        if(article.getStatus()==0 && !article.getAuthorId().equals(UserContext.get())) {
            throw new NotFoundException("文章不存在");
        }

        // 3.写入缓存,并设置 TTL(定期过期,防止缓存没有被正常删除而导致脏数据)
        redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(article), 10, TimeUnit.MINUTES);
        log.info("写入缓存: {}", key);

        return articleQueryService.buildArticleResponse(article, key);
    }

    @Override
    public void deleteArticleById(Long id) {
        Article article = this.getOptById(id).orElseThrow(() -> new NotFoundException("文章不存在,无法删除"));
        if (article.getAuthorId() != UserContext.get()) {
            throw new ForbiddenException("不能删除别人的文章");
        }
        //删文章后,相关的评论直接全部删除
        LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<Comment>();
        commentWrapper.eq(Comment::getArticleId, id);
        commentService.remove(commentWrapper);
        LambdaQueryWrapper<ArticleTag> tagWrapper = new LambdaQueryWrapper<>();
        tagWrapper.eq(ArticleTag::getArticleId, id);
        articleTagService.remove(tagWrapper);
        this.removeById(id);

        // 每次删除要删除缓存
        redisTemplate.delete("article:" + id);
        // 从 ES 库中删去
        articleSearchService.deleteArticle(id);
        log.info("删除缓存: article: {}", article);
    }

    @Override
    public void updateArticleById(ArticleRequest request) {
        Long id = request.getId();
        Article article = this.getOptById(id).orElseThrow(() -> new NotFoundException("文章不存在"));
        if (article.getAuthorId() != UserContext.get()) {
            throw new ForbiddenException("不能修改别人的文章");
        }

        int oldStatus = article.getStatus(); // 获取上一次编辑状态

        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setStatus(request.getStatus());
        if (request.getCategoryId() != null) {
            Category category = categoryService.getById(request.getCategoryId());
            if (category == null) {
                throw new NotFoundException("分类不存在");
            }
            article.setCategoryId(request.getCategoryId());
        }
        this.updateById(article);

        LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleTag::getArticleId, id);
        // 更新方式:先删除再插入
        articleTagService.remove(wrapper);
        List<Long> tagIds = request.getTagIds();
        if (tagIds != null && !tagIds.isEmpty()) {
            List<Tag> tags = tagService.listByIds(tagIds);
            if (tags.size() != tagIds.size()) {
                throw new NotFoundException("部分标签不存在");
            }
            for (Long tagId : tagIds) {
                ArticleTag articleTag = new ArticleTag();
                articleTag.setArticleId(article.getId());
                articleTag.setTagId(tagId);
                articleTagService.save(articleTag);
            }
        }

        // 每次更新要删除缓存
        redisTemplate.delete("article:" + id);
        // 根据发布状态决定是否存入 ES 库
        if(oldStatus == 1 && request.getStatus() == 0) { // 防止 ES 能查到某篇文章之前已发布但目前在草稿
            articleSearchService.deleteArticle(id);
        } else if (request.getStatus() == 1) {
            articleSearchService.syncArticle(article);
        }
        log.info("删除缓存: article: {}", id);
    }

    @Override
    public void adminDeleteArticleById(Long id) {
        userService.checkAdmin();
        Article article = this.getOptById(id)
                .orElseThrow(() -> new NotFoundException("文章不存在"));

        LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<>();
        commentWrapper.eq(Comment::getArticleId, id);
        commentService.remove(commentWrapper);

        LambdaQueryWrapper<ArticleTag> tagWrapper = new LambdaQueryWrapper<>();
        tagWrapper.eq(ArticleTag::getArticleId, id);
        articleTagService.remove(tagWrapper);

        this.removeById(id);
        redisTemplate.delete("article:" + id);
        // 从 ES库中删去
        articleSearchService.deleteArticle(id);
        log.info("管理员删除文章: {}", id);
    }

    @Override
    public void adminUpdateArticle(ArticleRequest request) {
        userService.checkAdmin();
        // 获取文章 id
        Long id = request.getId();
        if (id == null) {
            throw new NotFoundException("文章不存在");
        }

        Article article = this.getOptById(id).orElseThrow(() -> new NotFoundException("文章不存在"));
        int oldStatus = article.getStatus();

        article.setTitle(request.getTitle());
        article.setStatus(request.getStatus());

        article.setContent(request.getContent());
        if (request.getCategoryId() != null) {
            Category category = categoryService.getById(request.getCategoryId());
            if (category == null) {
                throw new NotFoundException("分类不存在");
            }
            article.setCategoryId(request.getCategoryId());
        }
        this.updateById(article);

        LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleTag::getArticleId, id);
        // 更新方式:先删除再插入

        articleTagService.remove(wrapper);
        List<Long> tagIds = request.getTagIds();
        if (tagIds != null && !tagIds.isEmpty()) {
            List<Tag> tags = tagService.listByIds(tagIds);
            if (tags.size() != tagIds.size()) {
                throw new NotFoundException("部分标签不存在");
            }
            for (Long tagId : tagIds) {
                ArticleTag articleTag = new ArticleTag();
                articleTag.setArticleId(article.getId());
                articleTag.setTagId(tagId);
                articleTagService.save(articleTag);
            }
        }

        // 每次更新要删除缓存
        redisTemplate.delete("article:" + id);
        // 根据发布状态决定是否存入 ES 库
        if (oldStatus == 1 && request.getStatus() == 0) {
            articleSearchService.deleteArticle(id);
        } else if (request.getStatus() == 1) {
            articleSearchService.syncArticle(article);
        }
        log.info("删除缓存: article: {}", id);
    }

    @Override
    public PageResponse<ArticleResponse> adminPageArticle(ArticleSearchRequest request) {
        userService.checkAdmin();
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        Page<Article> p = this.page(new Page<>(request.getPage(), request.getSize()), wrapper);
        List<ArticleResponse> list = BeanUtil.copyToList(p.getRecords(), ArticleResponse.class);
        PageResponse<ArticleResponse> response = new PageResponse<>();
        response.setTotal(p.getTotal());
        response.setList(list);
        return response;
    }
}
