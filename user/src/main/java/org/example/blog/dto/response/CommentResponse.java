package org.example.blog.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentResponse {

    private Long id;

    private String content;

    private Long articleId;

    private Long userId;

    private String userName;

    private String userAvatar;

    private LocalDateTime createTime;
}
