package org.example.blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateCommentRequest{

    @NotBlank(message = "评论不能为空")
    private String content;
}
