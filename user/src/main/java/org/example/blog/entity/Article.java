package org.example.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("article")
public class Article {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    private Long authorId;

    private LocalDateTime createTime;

    private Long categoryId;

    private Long viewCount;

    // getter 和 setter(使用 lombok 可以快速生成)
//    public long geitId(){ return id; }
//    public void setId(long id){ this.id=id; }
//
//    public String getTitle(){ return title; }
//    public void setTitle(String title){ this.title=title; }
//
//    public String getContent(){ return content; }
//    public void serContent(String content){ this.content=content; }
//
//    public long getAuthorId(){ return authorId; }
//    public void setAuthorId(long authorId){ this.authorId=authorId; }
//
//    public LocalDateTime getCreateTime(){ return createTime; }
//    public void setCreateTime(LocalDateTime createTime){ this.createTime=createTime; }
}
