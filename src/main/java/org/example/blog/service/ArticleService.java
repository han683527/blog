package org.example.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.blog.entity.Article;

import java.util.List;

public interface ArticleService extends IService<Article> {

    // 创建文章
    void createArticle(Long authorId,String title,String content);

    List<Article> getAllArticle(); //无条件,相当于 Select * from article

    // 根据 id 查找文章
    Article getArticleById(Long id);

//    // 删除所有文章(一般用不到)
//    void deleteAllArticle();

    // 根据 id 删除文章
    void deleteArticleById(Long id);

    // 修改文章
    void updateArticleById(Long id,String title,String content);

    // 分页
    IPage<Article> pageArticle(int page,int size);

    //按标题进行模糊查找
    IPage<Article> searchArticle(String keyword,int page,int size);
}
