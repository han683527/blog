package org.example.blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TagRequest {
    private Long TagId;

    @NotBlank(message = "标签名不能为空")
    private String tagName;
}
