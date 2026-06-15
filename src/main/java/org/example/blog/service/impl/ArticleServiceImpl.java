package org.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.blog.entity.Article;
import org.example.blog.entity.Comment;
import org.example.blog.mapper.ArticleMapper;
import org.example.blog.mapper.CommentMapper;
import org.example.blog.service.ArticleService;
import org.example.blog.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.NoSuchElementException;


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
    }

    @Override
    public List<Article> getAllArticle() {
        return this.list(); //查询全部,相当于 SQL 里的 select * from article
    }

    @Override
    public Article getArticleById(Long id) {
        Article article = this.getById(id);
        if (article == null) {
            throw new RuntimeException("文章不存在");
        }
        return article;
    }

//    @Override
//    public void deleteAllArticle() {
//        commentMapper.delete(null);
//        this.remove(null);
//    }

    @Override
    public void deleteArticleById(Long id) {
        Article article = this.getOptById(id)
                .orElseThrow(() -> new RuntimeException("文章不存在,无法删除"));
        if(article.getAuthorId() != UserContext.get()){
            throw new RuntimeException("不能删除别人的文章");
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
                .orElseThrow(() -> new NoSuchElementException("文章不存在"));
        if(article.getAuthorId() != UserContext.get()){
            throw new RuntimeException("不能修改别人的文章");
        }
        article.setTitle(title);
        article.setContent(content);
        this.updateById(article);
    }
}
