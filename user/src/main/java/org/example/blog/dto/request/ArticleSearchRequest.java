package org.example.blog.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class ArticleSearchRequest extends PageRequest {
    private String keyword;

    private Long categoryId;

    private List<Long> tagIds;
}
