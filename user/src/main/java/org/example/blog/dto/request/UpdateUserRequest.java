package org.example.blog.dto.request;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String nickname;

    private String oldPassword;

    private String newPassword;
}
