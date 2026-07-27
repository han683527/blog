package org.example.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("article_collect")
public class ArticleCollect {
    private Long userId;

    private Long articleId;

    private LocalDateTime createTime;
}
