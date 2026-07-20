package org.example.blog.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationResponse {
    private Long id;

    private Long actorId;

    private String actorNickname;

    // LIKE | COLLECT | COMMENT
    private String type;

    private Long articleId;

    private String articleTitle;

    private LocalDateTime createTime;

    private boolean readFlag;
}
