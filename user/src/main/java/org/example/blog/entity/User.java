package org.example.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("User") // mybatis-plus 注解,指定对应的表名
public class User {

    @TableId(type = IdType.AUTO) //标记主键
    private Long id;

    private String email;

    private String password;

    private String nickname;

    private LocalDateTime createTime;

    private String avatar;
}
