package org.example.blog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor //为类生成一个包含所有字段的构造方法
public class LoginResponse {
    private String accessToken;

    private String refreshToken;

    private String email;
}
