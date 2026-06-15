package org.example.blog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor //为类生成一个包含所有字段的构造方法
public class LoginResponse {

    private String token;

    private String email;

    //最好不暴露密码
//    private String password;

    private String nickname;
}
