package org.example.blog.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.blog.dto.response.ArticleResponse;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.entity.*;
import org.example.blog.exception.ForbiddenException;
import org.example.blog.exception.NotFoundException;
import org.example.blog.mapper.ArticleMapper;
import org.example.blog.mapper.ArticleTagMapper;
import org.example.blog.mapper.CommentMapper;
import org.example.blog.service.ArticleService;
import org.example.blog.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private ArticleTagMapper articleTagMapper;


    @Override
    public void createArticle(Long authorId, String title, String content,Long categoryId,List<Long> tagIds) {
        Article article = new Article();
        article.setAuthorId(authorId);
        article.setTitle(title);
        article.setContent(content);
        article.setCategoryId(categoryId);
        this.save(article);

        // 先有数据才能插标签,否则会读到 null
        if(tagIds != null && !tagIds.isEmpty()){
            for(Long tagId : tagIds){
                ArticleTag articleTag = new ArticleTag();
                articleTag.setArticleId(article.getId());
                articleTag.setTagId(tagId);
                articleTagMapper.insert(articleTag);
            }
        }


        log.info("用户 {} 创建文章 {}",UserContext.get(), title);
    }

    public Article getArticleById(Long id){
        // 1.先查缓存
        String key = "article:" + id; // 定义一个存取的钥匙
        String cached = redisTemplate.opsForValue().get(key);
        if(cached != null){
            log.info("缓存命中: {}",key);
            if("NULL".equals(cached)){
                throw new NotFoundException("文章不存在");
            }
            return JSONUtil.toBean(cached,Article.class);
        }

        // 2.缓存没有,查数据库
        Article article = this.getById(id);
        if(article == null){
            redisTemplate.opsForValue().set(key,"NULL",1,TimeUnit.MINUTES);
            log.info("写入空值缓存(防穿透): {}",key);
            throw new NotFoundException("文章不存在");
        }

        // 3.写入缓存,并设置 TTL(定期过期,防止缓存没有被正常删除而导致脏数据)
        redisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(article),10, TimeUnit.MINUTES);
        log.info("写入缓存: {}",key);
        return article;
    }

    @Override
    public PageResponse<ArticleResponse> searchArticleByTitleKeyword(String keyword,int page, int size){
        String key = "search:" + keyword;
        String cached = redisTemplate.opsForValue().get(key);
        if(cached != null){
            log.info("缓存命中: {}",key);
            // TypeReference 能保留类型信息让 JSON 反序列化正确
            return JSONUtil.toBean(cached,new TypeReference<PageResponse<ArticleResponse>>(){},false);
        }

        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Article::getTitle,keyword);
        Page<Article> p = this.page(new Page<>(page,size),wrapper);
        List<ArticleResponse> list = BeanUtil.copyToList(p.getRecords(),ArticleResponse.class);
        PageResponse<ArticleResponse> response = new PageResponse<>();
        response.setTotal(p.getTotal());
        response.setList(list);

        redisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(response),1, TimeUnit.MINUTES);
        log.info("写入缓存: {}",key);
        return response;
    }

    @Override
    public void deleteArticleById(Long id) {
        Article article = this.getOptById(id)
                .orElseThrow(() -> new NotFoundException("文章不存在,无法删除"));
        if(article.getAuthorId() != UserContext.get()){
            throw new ForbiddenException("不能删除别人的文章");
        }
        //删文章后,相关的评论直接全部删除
        LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<Comment>();
        commentWrapper.eq(Comment::getArticleId,id);
        commentMapper.delete(commentWrapper);
        LambdaQueryWrapper<ArticleTag>  tagWrapper = new LambdaQueryWrapper<>();
        tagWrapper.eq(ArticleTag::getArticleId,id);
        articleTagMapper.delete(tagWrapper);
        this.removeById(id);

        // 每次删除要删除缓存
        redisTemplate.delete("article:" + id);
        log.info("删除缓存: article: {}",article);
    }

    @Override
    public void updateArticleById(Long id, String title, String content,Long  categoryId,List<Long> tagIds) {
        Article article =  this.getOptById(id)
                .orElseThrow(() -> new NotFoundException("文章不存在"));
        if(article.getAuthorId() != UserContext.get()){
            throw new ForbiddenException("不能修改别人的文章");
        }
        article.setTitle(title);
        article.setContent(content);
        article.setCategoryId(categoryId);
        this.updateById(article);
        LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleTag::getArticleId,id);
        // 更新方式:先删除再插入
        articleTagMapper.delete(wrapper);
        if(tagIds != null && !tagIds.isEmpty()){
            for(Long tagId : tagIds){
                ArticleTag articleTag = new ArticleTag();
                articleTag.setArticleId(article.getId());
                articleTag.setTagId(tagId);
                articleTagMapper.insert(articleTag);
            }
        }

        // 每次更新要删除缓存
        redisTemplate.delete("article:" + id);
        log.info("删除缓存: article: {}",id);
    }

    @Override
    public PageResponse<ArticleResponse> pageArticle(int page, int size,Long categoryId) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        if(categoryId != null){
            wrapper.eq(Article::getCategoryId,categoryId);
        }
        Page<Article> p = this.page(new Page<>(page,size),wrapper);
        // Hutool 的根据方法:内部调用反射遍历 Article 的 getter,找到 ArticleResponse 里同名的字段,复制过去
        List<ArticleResponse> list = BeanUtil.copyToList(p.getRecords(),ArticleResponse.class);
        PageResponse<ArticleResponse> response = new PageResponse<>();
        response.setTotal(p.getTotal());
        response.setList(list);
        return response;
    }
}
