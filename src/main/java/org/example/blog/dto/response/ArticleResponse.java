package org.example.blog.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArticleResponse {

    private Long id;

    private String title;

    private String content;

    private Long authorId;

    private LocalDateTime createTime;
}
