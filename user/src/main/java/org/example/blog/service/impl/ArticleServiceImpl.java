package org.example.blog.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.blog.dto.response.ArticleResponse;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.entity.Article;
import org.example.blog.entity.Comment;
import org.example.blog.exception.ForbiddenException;
import org.example.blog.exception.NotFoundException;
import org.example.blog.mapper.ArticleMapper;
import org.example.blog.mapper.CommentMapper;
import org.example.blog.service.ArticleService;
import org.example.blog.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    @Autowired
    private CommentMapper commentMapper;

    @Override
    public void createArticle(Long authorId, String title, String content) {
        Article article = new Article();
        article.setAuthorId(authorId);
        article.setTitle(title);
        article.setContent(content);
        this.save(article);
        log.info("用户 {} 创建文章 {}",UserContext.get(), title);
    }

    @Override
    public Article getArticleById(Long id) {
        Article article = this.getById(id);
        if (article == null) {
            throw new NotFoundException("文章不存在");
        }
        return article;
    }

    @Override
    public PageResponse<ArticleResponse> searchArticle(String keyword, int page, int size) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Article::getTitle,keyword);
        Page<Article> p = this.page(new Page<>(page,size),wrapper);
        List<ArticleResponse> list = BeanUtil.copyToList(p.getRecords(),ArticleResponse.class);
        PageResponse<ArticleResponse> response = new PageResponse<>();
        response.setTotal(p.getTotal());
        response.setList(list);
        return response;
    }

//    @Override
//    public void deleteAllArticle() {
//        commentMapper.delete(null);
//        this.remove(null);
//    }

    @Override
    public void deleteArticleById(Long id) {
        Article article = this.getOptById(id)
                .orElseThrow(() -> new NotFoundException("文章不存在,无法删除"));
        if(article.getAuthorId() != UserContext.get()){
            throw new ForbiddenException("不能删除别人的文章");
        }
        //删文章后,相关的评论直接全部删除
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<Comment>();
        wrapper.eq(Comment::getArticleId,id);
        commentMapper.delete(wrapper);
        this.removeById(id);
    }

    @Override
    public void updateArticleById(Long id, String title, String content) {
        Article article =  this.getOptById(id)
                .orElseThrow(() -> new NotFoundException("文章不存在"));
        if(article.getAuthorId() != UserContext.get()){
            throw new ForbiddenException("不能修改别人的文章");
        }
        article.setTitle(title);
        article.setContent(content);
        this.updateById(article);
    }

    @Override
    public PageResponse<ArticleResponse> pageArticle(int page, int size) {
        Page<Article> p = this.page(new Page<>(page,size));
        // Hutool 的根据方法:内部调用反射遍历 Article 的 getter,找到 ArticleResponse 里同名的字段,复制过去
        List<ArticleResponse> list = BeanUtil.copyToList(p.getRecords(),ArticleResponse.class);
        PageResponse<ArticleResponse> response = new PageResponse<>();
        response.setTotal(p.getTotal());
        response.setList(list);
        return response;
    }
}
