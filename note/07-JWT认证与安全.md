# JWT 认证与安全

## 认证流程

```
客户端                          服务端
  │                               │
  │   POST /user/login            │
  │  {email, password}            │
  │ ─────────────────────────→    │  校验密码
  │                               │  生成 accessToken + refreshToken
  │   {accessToken, refreshToken, │
  │    email}                     │
  │ ←─────────────────────────    │
  │                               │
  │   GET /article（带 Token）     │
  │  Authorization: Bearer xxx    │
  │ ─────────────────────────→    │  TokenInterceptor
  │                               │  解析 token → 查黑名单 → 取出 userId
  │                               │  存入 UserContext
  │                               │  放行 → Controller
  │   {code:200, data:...}        │
  │ ←─────────────────────────    │
  │                               │
  │   POST /user/refresh          │
  │  {refreshToken}               │
  │ ─────────────────────────→    │  校验 refresh token
  │                               │  拉黑旧 refresh token
  │                               │  生成新 accessToken + refreshToken
  │   {newAccessToken,            │
  │    newRefreshToken, email}    │
  │ ←─────────────────────────    │
```

## 双 Token 机制（Access Token + Refresh Token）

### 为什么需要两个 Token

单 token 面临两难：有效期短 → 用户频繁重新登录体验差；有效期长 → 泄漏后风险大。

双 token 解决思路：给一个**短期凭证**访问资源 + 一个**长期凭证**用来续期。

### 对比

| | Access Token | Refresh Token |
|---|---|---|
| 有效期 | 15 分钟 | 7 天 |
| 携带方式 | 每次请求的 Authorization Header | 仅刷新时传输 |
| 包含字段 | userId + email + jti | userId + jti |
| 用途 | 访问受保护的资源 | 获取新的 Access Token |

### 刷新流程

```java
public LoginResponse refresh(RefreshTokenRequest request) {
    // 1. 校验 refresh token 是否有效（签名 + 过期 + 黑名单）
    Long userId = jwtUtil.validateAndGetUserId(refreshToken);

    // 2. 拉黑旧 refresh token（防止重放）
    jwtUtil.blacklistToken(refreshToken);

    // 3. 签发新的双 token
    String newAccessToken = jwtUtil.generateAccessToken(userId, email);
    String newRefreshToken = jwtUtil.generateRefreshToken(userId);
    return new LoginResponse(newAccessToken, newRefreshToken, email);
}
```

**为什么要轮换 refresh token？** 如果 refresh token 泄漏而不轮换，攻击者可以无限续期。每次刷新都发新的、拉黑旧的，泄漏的 token 只能用一次。

## JwtUtil：静态工具类 → Spring 组件

### 原来的问题

```java
// ❌ 改造前：静态方法，硬编码
public class JwtUtil {
    private static final String SECRET = "hardcoded-key";
    private static final long EXPIRE = 86400000;

    public static String generateToken(...) { ... }
}
```

- 密钥硬编码在代码里，不同环境要改代码
- 静态方法无法注入 Spring Bean（RedisTemplate）
- 无法扩展黑名单等功能

### 改造后

```java
@Component  // 注册到 Spring 容器
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expire}")
    private long accessTokenExpire;

    @Autowired
    private StringRedisTemplate redisTemplate;

    public String generateAccessToken(Long userId, String email) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("jti", UUID.randomUUID().toString())  // 唯一标识
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpire))
                .signWith(getKey())
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("jti", UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpire))
                .signWith(getKey())
                .compact();
    }

    public Long validateAndGetUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        // 检查是否在黑名单中
        String jti = claims.get("jti", String.class);
        if (Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:jti:" + jti))) {
            throw new RuntimeException("Token 已失效");
        }
        return Long.parseLong(claims.getSubject());
    }

    public void blacklistToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        String jti = claims.get("jti", String.class);
        long ttl = claims.getExpiration().getTime() - System.currentTimeMillis();
        if (ttl > 0) {
            redisTemplate.opsForValue().set(
                "blacklist:jti:" + jti, "1", ttl, TimeUnit.MILLISECONDS);
        }
    }
}
```

### 组件化的好处

1. **依赖注入** — `StringRedisTemplate` 由 Spring 自动注入
2. **配置外移** — 密钥、有效期等从 `application.yml` 读取
3. **方便扩展** — 加黑名单等功能直接添加方法

## Token 黑名单（主动注销）

### 原理

```java
// 每个 token 生成时带一个唯一的 UUID
.claim("jti", UUID.randomUUID().toString())

// 登出时把 jti 存入 Redis，TTL = token 剩余有效期
redisTemplate.opsForValue().set("blacklist:jti:" + jti, "1", ttl, TimeUnit.MILLISECONDS);

// 每次请求校验时查 Redis
if (redisTemplate.hasKey("blacklist:jti:" + jti)) {
    throw new RuntimeException("Token 已失效");
}
```

### 为什么用 Redis

| | Redis | 数据库 |
|---|---|---|
| 查询速度 | 内存操作，微秒级 | 磁盘 IO，毫秒级 |
| TTL 过期 | 原生支持 | 需要定时任务清理 |
| 适合场景 | 高频、短生命周期数据 | 持久化数据 |

### 为什么不直接存 token 本身

存 token 字符串太长（几百字符），存 `jti`（UUID）只有 36 字符，节省内存和带宽。

### TTL 计算

```java
long ttl = claims.getExpiration().getTime() - System.currentTimeMillis();
if (ttl > 0) {
    redisTemplate.opsForValue().set("blacklist:jti:" + jti, "1", ttl, TimeUnit.MILLISECONDS);
}
```

- TTL 跟 token 剩余有效期一致，token 过期 Redis 自动删除
- 如果 token 已过期（ttl <= 0），不需要存

### Token 解码后的内容

**Access Token：**
```json
{
  "sub": "1",          // userId
  "email": "a@b.com",
  "jti": "uuid-xxx",   // 唯一标识（用于黑名单）
  "iat": 1700000000,   // 签发时间
  "exp": 1700000900    // 过期时间（15 分钟）
}
```

**Refresh Token：**
```json
{
  "sub": "1",          // userId
  "jti": "uuid-yyy",   // 唯一标识
  "iat": 1700000000,
  "exp": 1700604800    // 过期时间（7 天）
}
```

## TokenInterceptor 改造

### 改造前

```java
public class TokenInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(...) {
        // 静态方法调用
        Long userId = JwtUtil.getUserId(token);
        UserContext.set(userId);
        return true;
    }
}
```

### 改造后

```java
@Component
@RequiredArgsConstructor
public class TokenInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;  // 注入组件

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

        token = token.substring(7);

        // 2. 验证 token（解析 + 黑名单检查）
        try {
            Long userId = jwtUtil.validateAndGetUserId(token);
            UserContext.set(userId);
            return true;
        } catch (Exception e) {
            response.setStatus(401);
            response.getWriter().write("token无效或已过期");
            return false;
        }
    }
}
```

### 核心变化

- 不再调用静态方法，而是注入 `JwtUtil` 实例
- `validateAndGetUserId()` 内部会查 Redis 黑名单，被拉黑的 token 直接拒绝
- 使用 `@RequiredArgsConstructor` 构造器注入（跟 Controller/Service 风格一致）

## 配置外移

### application.yml

```yaml
jwt:
  secret: blog-secret-key-123456789012345678901234567890  # HMAC-SHA256 要求至少 32 字符
  access-token-expire: 900000                              # 15 分钟（毫秒）
  refresh-token-expire: 604800000                          # 7 天（毫秒）
```

### 配置管理

```yaml
# 本地配置（不提交 Git）
application.yml

# 模板配置（提交 Git，敏感信息用占位符）
application-example.yml
```

### 密钥要求

使用 HMAC-SHA256 算法，密钥**至少 256 位**（32 个 UTF-8 字符），不够会抛异常。

## Logout 实现

```java
// Controller — 只做路由，无业务逻辑
@PostMapping("/logout")
public Result<String> logout(@RequestHeader("Authorization") String authHeader) {
    userService.logout(authHeader);
    return Result.success("登出成功");
}

// Service — 实际业务
public void logout(String authHeader) {
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
        jwtUtil.blacklistToken(authHeader.substring(7));
        // token 被拉黑后，即使未过期也无法通过拦截器
    }
}
```

## 拦截器注册与白名单

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final TokenInterceptor tokenInterceptor;

    public WebConfig(TokenInterceptor tokenInterceptor) {
        this.tokenInterceptor = tokenInterceptor;
    }

    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                    "/user/register",   // 注册不需要登录
                    "/user/login",      // 登录不需要登录
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                );
    }
}
```

注意：`/user/refresh` 和 `/user/logout` 不在白名单中，因为它们需要 token 认证。

## 涉及的其他知识点

### @Component 注解

```java
@Component  // 把类注册到 Spring 容器，让 Spring 管理生命周期
public class JwtUtil { ... }
```

- `@Service`、`@RestController`、`@Repository` 底层都是 `@Component`
- 没有这个注解，`@Autowired`、`@Value` 不会生效
- 加了之后，Spring 启动时自动创建实例（Bean），注入到需要的地方

### DTO 设计 — RefreshTokenRequest

```java
@Data
public class RefreshTokenRequest {
    @NotBlank(message = "refreshToken 不能为空")
    private String refreshToken;
}
```

专门给刷新接口用，只接收 refreshToken 字段，跟 LoginRequest 分开。

### LoginResponse 变化

```java
// 改造前
@Data
public class LoginResponse {
    private String token;
    private String email;
    private String nickname;
}

// 改造后
@Data
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String email;
}
```

## 容易踩的坑

1. **`jti` 拼成 `jtl`** — 黑名单永远查不到对应的 key，token 永远不过期
2. **`TimeUnit.MILLISECONDS` 写成 `MINUTES`** — 本应存 15 分钟变成存 15 毫秒，token 立刻"过期"（Redis 查不到）
3. **`@Autowired` 忘记加** — `JwtUtil` 或 `redisTemplate` 为 null，调用时抛 `NullPointerException`
4. **密钥长度不够** — HMAC-SHA256 需要至少 256 位（32 字符），否则抛异常
5. **Controller 返回类型不匹配** — Service 返回 `LoginResponse`，Controller 得写 `Result<LoginResponse>` 不是 `Result<User>`
