package org.example.blog.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PageRequest {
    @Schema(example = "1")
    private int page;

    @Schema(example = "10")
    private int size;
}
