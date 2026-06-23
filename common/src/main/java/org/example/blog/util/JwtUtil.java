package org.example.blog.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

// 在登录后生成一个 token(包含 id,email 等信息),当用户在进行非登录操作时,
// 能够用 token 验证是谁发的(因为 HTTP 协议是无状态的,你登录后如果没有 token 或是 session 接下来的操作它不知道是谁在做)
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expire}")
    private long accessTokenExpire;

    @Value("${jwt.refresh-token-expire}")
    private long refreshTokenExpire;

    @Autowired
    private StringRedisTemplate redisTemplate;

    public SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Long userId, String email) {
        return Jwts.builder()          //概括接下来要做什么:创建一个 token
                .subject(userId.toString())    //把 id 放进 token
                .claim("email", email)       //自定义字段(额外要放进 token 的信息)
                .claim("jti", UUID.randomUUID().toString())
                .issuedAt(new Date())          //签发时间(创建 token 的时间)
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpire))  //token 过期时间(当前时间 + 规定的持续时间 EXPIRE)
                .signWith(getKey())            //用密钥签名
                .compact();                    //压缩成字符串
    }

    public String generateRefreshToken(Long userId) {
        return Jwts.builder()                  //概括接下来要做什么:创建一个 token
                .subject(userId.toString())       //自定义字段(额外要放进 token 的信息)
                .claim("jti", UUID.randomUUID().toString())
                .issuedAt(new Date())          //签发时间(创建 token 的时间)
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpire))  //token 过期时间(当前时间 + 规定的持续时间 EXPIRE)
                .signWith(getKey())            //用密钥签名
                .compact();                    //压缩成字符串
    }

    public Long validateAndGetUserId(String token) {
        Claims claims = Jwts.parser()          //概括接下来要做什么:解析(parser)token
                .verifyWith(getKey())          //用密钥验证签名
                .build()                       //开始构建解析器
                .parseSignedClaims(token)      //解析 token
                .getPayload();                 //取出 token 中的数据

        String jti = claims.get("jti",String.class);
        if(Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:jti:"+ jti))){
            throw new RuntimeException("Token 已失效");
        }
        return Long.parseLong(claims.getSubject());  //获取到 userId
    }

    public void blacklistToken(String token) {
        Claims claims = Jwts.parser()          //概括接下来要做什么:解析(parser)token
                .verifyWith(getKey())          //用密钥验证签名
                .build()                       //开始构建解析器
                .parseSignedClaims(token)      //解析 token
                .getPayload();                 //取出 token 中的数据
        String jti = claims.get("jti",String.class);
        long ttl = claims.getExpiration().getTime() - System.currentTimeMillis();
        if(ttl > 0){
            redisTemplate.opsForValue().set("blacklist:jti:" + jti,"1",ttl,TimeUnit.MILLISECONDS);
        }
    }
}
