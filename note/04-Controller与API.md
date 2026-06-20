# Controller 与 API

## 职责

接收 HTTP 请求、解析参数、调用 Service、返回响应。

```java
@RestController
@RequestMapping("/article")
@RequiredArgsConstructor
public class ArticleController {
    private final ArticleService articleService;
}
```

## 路由注解

| 注解 | HTTP 方法 | 语义 |
|---|---|---|
| `@GetMapping` | GET | 查询 |
| `@PostMapping` | POST | 新增 |
| `@PutMapping` | PUT | 全量修改 |
| `@DeleteMapping` | DELETE | 删除 |

**RESTful 设计：**
```
GET    /article          → 查询列表
GET    /article/{id}     → 查询单个
POST   /article          → 新增
PUT    /article/{id}     → 修改
DELETE /article/{id}     → 删除
```

## 参数注解

```java
// 从 URL 路径取值：GET /article/3  → id=3
@PathVariable Long id

// 从查询字符串取值：GET /article?page=1&size=10
@RequestParam(defaultValue = "1") int page

// 从请求体取 JSON：POST /article  body: {"title":"...", "content":"..."}
@RequestBody

// 触发参数校验
@Valid
```

### @RequestParam vs @PathVariable

**URL 示例：** `/article/search/spring?page=1&size=10`

```java
// @PathVariable 取 → spring
// @RequestParam 取 → page=1, size=10
@GetMapping("/search/{keyword}")
public Result<...> search(@PathVariable String keyword,
                          @RequestParam int page,
                          @RequestParam int size)
```

`@PathVariable` 取路径段，`@RequestParam` 取 `?` 后面的参数。

## 依赖注入

```java
@RequiredArgsConstructor // 为 final 字段生成构造器，Spring 自动注入
public class ArticleController {
    private final ArticleService articleService;
}
```

等价于：

```java
public ArticleController(ArticleService articleService) {
    this.articleService = articleService;
}
```

## HTTP 状态码

| 状态码 | 含义 | 使用场景 |
|---|---|---|
| 200 | OK | 请求成功 |
| 201 | Created | 新增成功 |
| 400 | Bad Request | 参数错误 |
| 401 | Unauthorized | 未登录 / token 无效 |
| 403 | Forbidden | 没有权限（修改别人的文章） |
| 404 | Not Found | 资源不存在 |
| 500 | Internal Server Error | 服务器内部错误 |

目前项目统一返回 200（body 中 code 区分成功失败），后续可以优化为按场景返回对应状态码。
