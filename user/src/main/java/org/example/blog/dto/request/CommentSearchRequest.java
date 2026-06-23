package org.example.blog.dto.request;

import lombok.Data;

@Data
public class CommentSearchRequest extends PageRequest{
    private Long userId;

    private Long articleId;
}

