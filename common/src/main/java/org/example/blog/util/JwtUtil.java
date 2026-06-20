package org.example.blog.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

// 在登录后生成一个 token(包含 id,email 等信息),当用户在进行非登录操作时,
// 能够用 token 验证是谁发的(因为 HTTP 协议是无状态的,你登录后如果没有 token 或是 session 接下来的操作它不知道是谁在做)
public class JwtUtil {
    private static final String SECRET = "blog-secret-key-123456789012345678901234567890"; //至少32位
    private static final long EXPIRE = 24*60*60*1000; //24小时 ms 为单位

    //生成密钥
    //用上方定义的 SECRET 字符串生成一个加密密钥
    //这个密钥在签名 signWith 和验证verifyWith 时会用到
    public static SecretKey getKey(){
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }


    //生成 Token
    public static String generateToken(Long userId, String email){
        return Jwts.builder()                  //概括接下来要做什么:创建一个 token
                .subject(userId.toString())    //把 id 放进 token
                .claim("email",email)       //自定义字段(额外要放进 token 的信息)
                .issuedAt(new Date())          //签发时间(创建 token 的时间)
                .expiration(new Date(System.currentTimeMillis() + EXPIRE))  //token 过期时间(当前时间 + 规定的持续时间 EXPIRE)
                .signWith(getKey())            //用密钥签名
                .compact();                    //压缩成字符串
    }

    // 从 token 中解析用户 id
    public static Long getUserId(String token){
        Claims claims = Jwts.parser()          //概括接下来要做什么:解析(parser)token
                .verifyWith(getKey())          //用密钥验证签名
                .build()                       //开始构建解析器
                .parseSignedClaims(token)      //解析 token
                .getPayload();                 //取出 token 中的数据
        return Long.parseLong(claims.getSubject());  //获取到 userId
    }
}
