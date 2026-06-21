package org.example.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("Comment")
public class Comment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String content;

    private Long userId;

    private Long articleId;

    private LocalDateTime createTime;

//    public long getId() { return id; }
//    public void setId(long id) { this.id = id; }
//
//    public String getContent() { return content; }
//    public void setContent(String content) { this.content = content; }
//
//    public long getUserId() { return userId; }
//    public void setUserId(long userId) { this.userId = userId; }
//
//    public long getArticleId() { return articleId; }
//    public void setArticleId(long articleId) { this.articleId = articleId; }
//
//    public LocalDateTime getCreateTime() { return createTime; }
//    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
