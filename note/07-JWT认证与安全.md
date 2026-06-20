# JWT 认证与安全

## 认证流程

```
客户端                          服务端
  │                               │
  │   POST /user/login            │
  │  {email, password}            │
  │ ─────────────────────────→    │  校验密码
  │                               │  生成 JWT token
  │   {token, email, nickname}    │
  │ ←─────────────────────────    │
  │                               │
  │   GET /article（带 Token）     │
  │  Authorization: Bearer xxx    │
  │ ─────────────────────────→    │  TokenInterceptor
  │                               │  解析 token → 取出 userId
  │                               │  存入 UserContext
  │                               │  放行 → Controller
  │   {code:200, data:...}        │
  │ ←─────────────────────────    │
```

## 拦截器（Interceptor）

拦截器在每个请求进入 Controller 之前校验 token：

```java
@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String token = request.getHeader("Authorization");

        // 1. 检查是否有 token
        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(401);
            response.getWriter().write("未登录");
            return false;
        }

        token = token.substring(7); // 去掉 "Bearer "

        // 2. 验证 token
        try {
            Long userId = JwtUtil.getUserId(token);
            UserContext.set(userId);  // 存入 ThreadLocal
            return true;              // 放行
        } catch (Exception e) {
            response.setStatus(401);
            response.getWriter().write("token无效或已过期");
            return false;
        }
    }
}
```

### 注册拦截器 + 白名单

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                    "/user/register",
                    "/user/login",
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                );
    }
}
```

白名单里的请求不需要 token，其他全部需要。

## JWT 工具类

### 生成 Token

```java
public static String generateToken(Long userId, String email) {
    return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + EXPIRE))
            .signWith(getKey())
            .compact();
}
```

### 解析 Token

```java
public static Long getUserId(String token) {
    Claims claims = Jwts.parser()
            .verifyWith(getKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    return Long.parseLong(claims.getSubject());
}
```

### Token 解码后的内容

```json
{
  "sub": "1",         // userId
  "email": "a@b.com",
  "iat": 1700000000,  // 签发时间
  "exp": 1700086400   // 过期时间（24小时）
}
```

## ThreadLocal 用户上下文

```java
public class UserContext {
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    public static void set(Long userId) { USER_ID.set(userId); }
    public static Long get() { return USER_ID.get(); }
    public static void remove() { USER_ID.remove(); }
}
```

**原理：** 每个线程有独立副本。Web 应用中一个请求全程由一个线程处理，所以 Interceptor 设置的值，Controller 和 Service 都能读到。

**权限校验：**
```java
if (article.getAuthorId() != UserContext.get()) {
    throw new RuntimeException("不能删除别人的文章");
}
```

## BCrypt 密码加密

```java
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

// 注册时加密
String encoded = encoder.encode(password);
user.setPassword(encoded);

// 登录时验证
boolean match = encoder.matches(rawPassword, storedPassword);
```

**BCrypt vs MD5：**
- MD5 固定结果，彩虹表可破解
- BCrypt 每次结果不同（自动加盐），计算慢，暴力破解成本高

## 客户端调用方式

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

所有需要认证的请求在 Header 中带上 `Authorization: Bearer {token}`。Postman 测试时在 Headers 里加这个字段。
