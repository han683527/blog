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

## RESTful 设计风格

### 核心思想

**URL 表示"是什么资源"，HTTP 方法表示"做什么操作"。**

```
非 RESTful：
GET  /getArticle?id=3
POST /deleteArticle
POST /createArticle

RESTful：
GET    /article/3     → 查文章3
DELETE /article/3     → 删文章3
POST   /article       → 新增文章
```

URL 只表达"什么东西"，方法表达"干什么"。

## 路由注解

| 注解 | HTTP 方法 | 语义 |
|---|---|---|
| `@GetMapping` | GET | 查询 |
| `@PostMapping` | POST | 新增 |
| `@PutMapping` | PUT | 全量修改 |
| `@DeleteMapping` | DELETE | 删除 |

**本项目 API 设计：**
```
GET    /article              → 文章列表
GET    /article/{id}         → 文章详情
GET    /article/search/{kw}  → 搜索文章
POST   /article              → 新增文章
PUT    /article/{id}         → 修改文章
DELETE /article/{id}         → 删除文章
GET    /article/{id}/comments → 文章下的评论（子资源）
GET    /comment              → 评论列表
POST   /comment              → 新增评论
PUT    /comment/{id}         → 修改评论
DELETE /comment/{id}         → 删除评论
```

`/article/{id}/comments` 这种路径表示评论是文章的子资源，URL 读起来就是"文章3的评论"。新增评论不需要在 URL 里写文章 ID，因为文章 ID 在请求体里。

### 子资源

资源可以嵌套，表达从属关系：

```
GET /article/3/comments   → 查看文章3下的评论
GET /user/1/articles      → 查看用户1的文章
```

嵌套的层级不宜太深，一般不超过两级。

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
