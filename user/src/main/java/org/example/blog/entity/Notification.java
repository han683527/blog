package org.example.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notification")
public class Notification {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long actorId;

    private String type;

    private Long articleId;

    private LocalDateTime createTime;

    @TableField("is_read")
    private boolean readFlag;
}
