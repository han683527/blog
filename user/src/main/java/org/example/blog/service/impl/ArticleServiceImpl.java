package org.example.blog.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.blog.dto.request.ArticleRequest;
import org.example.blog.dto.request.ArticleSearchRequest;
import org.example.blog.dto.response.ArticleResponse;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.entity.*;
import org.example.blog.exception.ForbiddenException;
import org.example.blog.exception.NotFoundException;
import org.example.blog.mapper.*;
import org.example.blog.service.ArticleService;
import org.example.blog.util.UserContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    private final StringRedisTemplate redisTemplate;

    private final CommentMapper commentMapper;

    private final ArticleTagMapper articleTagMapper;

    private final TagMapper tagMapper;

    private final CategoryMapper categoryMapper;

    @Override
    public void createArticle(ArticleRequest request) {
        Long userId = UserContext.get();
        Article article = new Article();
        String title = request.getTitle();
        if (request.getId() == null) {
            article.setAuthorId(userId);
            article.setTitle(title);
            article.setContent(request.getContent());

            // 判断分类是否存在
            Category category = categoryMapper.selectById(request.getCategoryId());
            if(category == null){
                throw new NotFoundException("分类不存在");
            }
            article.setCategoryId(request.getCategoryId());
            this.save(article);
        }

        // 先有数据才能插标签,否则会读到 null
        List<Long> tagIds = request.getTagIds();

        // 判断标签是否存在
        if (tagIds != null && !tagIds.isEmpty()) {
            List<Tag> tags = tagMapper.selectByIds(tagIds);
            if(tags.size() != tagIds.size()){
                throw new NotFoundException("部分标签不存在");
            }
            for (Long tagId : tagIds) {

                ArticleTag articleTag = new ArticleTag();
                articleTag.setArticleId(article.getId());
                articleTag.setTagId(tagId);
                articleTagMapper.insert(articleTag);
            }
        }

        log.info("用户 {} 创建文章 {}", userId, title);
    }

    @Override
    public PageResponse<ArticleResponse> pageArticle(ArticleSearchRequest request) {
        // 1.什么都不做 ; 2.模糊查找 ; 3.种类 ; 4.标签
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();

        // 模糊查找
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

        // 标签
        List<Long> tagIds = request.getTagIds();
        if (tagIds != null && !tagIds.isEmpty()) {
            List<ArticleTag> articleTags = articleTagMapper.selectList(new LambdaQueryWrapper<ArticleTag>().in(ArticleTag::getTagId, tagIds));
            List<Long> articleIds = articleTags.stream().map(ArticleTag::getArticleId).distinct().collect(Collectors.toList());
            wrapper.in(Article::getId, articleIds);
        }

        // 如果上述条件均不触发就只执行分页查找
        int page = request.getPage();
        int size = request.getSize();
        Page<Article> p = this.page(Page.of(page, size), wrapper);
        // Hutool 的根据方法:内部调用反射遍历 Article 的 getter,找到 ArticleResponse 里同名的字段,复制过去
        List<ArticleResponse> list = BeanUtil.copyToList(p.getRecords(), ArticleResponse.class);
        // 查找标签
        List<Long> articleIdList = p.getRecords().stream()
                .map(Article::getId).
                        collect(Collectors.toList());
        if (!articleIdList.isEmpty()) {
            List<ArticleTag> allTags = articleTagMapper.selectList(new LambdaQueryWrapper<ArticleTag>().in(ArticleTag::getArticleId, articleIdList));
            Map<Long, List<Long>> tagMap = allTags.stream().collect(Collectors.groupingBy(ArticleTag::getArticleId, Collectors.mapping(ArticleTag::getTagId, Collectors.toList())));
            for (ArticleResponse response : list) {
                response.setTags(tagMap.getOrDefault(response.getId(), List.of()));
            }
        }

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
            Article article = BeanUtil.toBean(cached, Article.class);
            // 查标签
            List<ArticleTag> articleTags = articleTagMapper.selectList(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, article.getId()));
            List<Long> tagIds = articleTags.stream().map(ArticleTag::getTagId).collect(Collectors.toList());
            ArticleResponse response = BeanUtil.toBean(article, ArticleResponse.class);
            response.setTags(tagIds);
            return response;
        }

        // 2.缓存没有,查数据库
        Article article = this.getById(id);
        if (article == null) {
            redisTemplate.opsForValue().set(key, "NULL", 1, TimeUnit.MINUTES);
            log.info("写入空值缓存(防穿透): {}", key);
            throw new NotFoundException("文章不存在");
        }

        // 3.写入缓存,并设置 TTL(定期过期,防止缓存没有被正常删除而导致脏数据)
        redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(article), 10, TimeUnit.MINUTES);
        log.info("写入缓存: {}", key);

        // 查询标签
        List<ArticleTag> articleTags = articleTagMapper.selectList(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, article.getId()));
        List<Long> tagIds = articleTags.stream().map(ArticleTag::getTagId).collect(Collectors.toList());

        ArticleResponse response = BeanUtil.toBean(article, ArticleResponse.class);
        response.setTags(tagIds);
        return response;
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
        commentMapper.delete(commentWrapper);
        LambdaQueryWrapper<ArticleTag> tagWrapper = new LambdaQueryWrapper<>();
        tagWrapper.eq(ArticleTag::getArticleId, id);
        articleTagMapper.delete(tagWrapper);
        this.removeById(id);

        // 每次删除要删除缓存
        redisTemplate.delete("article:" + id);
        log.info("删除缓存: article: {}", article);
    }

    @Override
    public void updateArticleById(ArticleRequest request) {
        Long id = request.getId();
        Article article = this.getOptById(id).orElseThrow(() -> new NotFoundException("文章不存在"));
        if (article.getAuthorId() != UserContext.get()) {
            throw new ForbiddenException("不能修改别人的文章");
        }

        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        Category category = categoryMapper.selectById(request.getCategoryId());
        if(category == null){
            throw new NotFoundException("分类不存在");
        }
        article.setCategoryId(request.getCategoryId());
        this.updateById(article);

        LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleTag::getArticleId, id);
        // 更新方式:先删除再插入
        articleTagMapper.delete(wrapper);
        List<Long> tagIds = request.getTagIds();
        if (tagIds != null && !tagIds.isEmpty()) {
            List<Tag> tags = tagMapper.selectByIds(tagIds);
            if(tags.size() != tagIds.size()){
                throw new NotFoundException("部分标签不存在");
            }
            for (Long tagId : tagIds) {
                ArticleTag articleTag = new ArticleTag();
                articleTag.setArticleId(article.getId());
                articleTag.setTagId(tagId);
                articleTagMapper.insert(articleTag);
            }
        }

        // 每次更新要删除缓存
        redisTemplate.delete("article:" + id);
        log.info("删除缓存: article: {}", id);
    }
}
