package org.example.blog.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArticleResponse {

    private Long id;

    private String title;

    private String content;

    private Long authorId;

    private Long categoryId;

    private List<Long> tags;

    private LocalDateTime createTime;

    private Long viewCount;

    private Long likeCount;

    private Long collectCount;

    private Long commentCount;
}
