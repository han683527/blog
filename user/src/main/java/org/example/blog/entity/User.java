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

    private LocalDateTime createTime; //这里用 Java 的驼峰命名,数据库建表是因为配置了 map-underscore-to-camel-case: true 自动映射

//    public long getId(){ return id; }
//    public void setId(long id){ this.id=id; }
//
//    public String getEmail(){ return email; }
//    public void setEmail(String email){ this.email=email; }
//
//    public String getPassword(){ return password; }
//    public void setPassword(String password){ this.password=password; }
//
//    public String getNickname(){ return nickname; }
//    public void setNickname(String nickname){ this.nickname=nickname; }
//
//    public LocalDateTime getCreateTime(){ return createTime; }
//    public void setCreateTime(LocalDateTime createTime){ this.createTime=createTime; }
}
