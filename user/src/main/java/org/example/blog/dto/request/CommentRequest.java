package org.example.blog.dto.request;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentRequest{
    private Long id;

    private Long articleId;

    @NotBlank(message = "评论不能为空")
    private String content;
}

