package org.example.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("Tag")
public class Tag {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String tagName;

    private LocalDateTime createTime;
}
