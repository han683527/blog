package org.example.blog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.example.blog.mapper") //1.在此处添加一个注解 扫描这个 mapper 包;2.或者在 mapper 中每个接口都加 @Mapper --为的是注册 Bean
public class BlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogApplication.class, args);
    }

}
