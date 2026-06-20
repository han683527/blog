# DTO 与参数校验

## 为什么要有 DTO

DTO（Data Transfer Object）是专门给接口用的数据结构，和 Entity 分开。

**Entity 对应数据库，DTO 对应接口。** 各自的变更互不影响。

### Request DTO

```java
@Data
public class RegisterRequest {
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "昵称不能为空")
    private String nickname;
}
```

**不用 Entity 接收请求的原因：**

1. 不需要的字段不会暴露（比如 User 的 id、createTime）
2. 不同接口的校验规则可以不同
3. 请求参数变化不会影响表结构

### Response DTO（VO）

```java
@Data
public class LoginResponse {
    private String token;
    private String email;
    private String nickname;
    // 不暴露 password
}
```

**控制返回给前端的字段：** 不需要的就不返回，比如密码、内部 ID 等。

## PageResponse — 分页包装

```java
@Data
public class PageResponse<T> {
    private long total;    // 总记录数
    private List<T> list;  // 当前页数据
}
```

相比 MyBatis-Plus 的 `IPage`（返回 records/total/pages/current/size），自定义只给前端需要的字段。

### Entity → VO 转换

```java
Page<Article> p = this.page(new Page<>(page, size));
List<ArticleVO> list = BeanUtil.copyToList(p.getRecords(), ArticleVO.class);
PageResponse<ArticleVO> response = new PageResponse<>();
response.setTotal(p.getTotal());
response.setList(list);
return response;
```

## Jackson 序列化

Spring Boot 自动用 Jackson 把 Java 对象转成 JSON 返回。控制序列化的注解：

```java
@Data
public class UserVO {
    @JsonIgnore
    private String password;          // 忽略此字段，不返回给前端

    @JsonProperty("user_name")
    private String nickname;          // 字段改名为 user_name 返回

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createTime; // 日期格式
}
```

## 参数校验

### 常用验证注解

| 注解 | 作用 | 适用类型 |
|---|---|---|
| `@NotBlank` | 不为 null，且去掉空格后长度 > 0 | String |
| `@NotEmpty` | 不为 null，且长度 > 0 | String / 集合 |
| `@NotNull` | 不为 null | 任意 |
| `@Email` | 邮箱格式 | String |
| `@Size(min, max)` | 长度范围 | String / 集合 |
| `@Min` / `@Max` | 数值范围 | 数值 |
| `@Pattern(regexp)` | 正则匹配 | String |

### 触发校验

```java
@PostMapping("/register")
public Result<String> register(@Valid @RequestBody RegisterRequest request) {
    // @Valid 触发校验
}
```

加上 `@Valid` 注解，校验失败时 Spring 自动返回 400 错误，不会进入方法体。
