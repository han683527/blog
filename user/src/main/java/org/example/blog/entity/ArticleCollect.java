package org.example.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("Article_Collect")
public class ArticleCollect {
    private Long userId;

    private Long articleId;

    private LocalDateTime createTime;
}
