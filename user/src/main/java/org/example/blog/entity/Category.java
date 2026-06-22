package org.example.blog.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Category {
    private Long id;

    private String categoryName;

    private LocalDateTime createTime;
}
