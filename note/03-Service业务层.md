# Service 业务层

## 职责

业务逻辑层，编排 CRUD 操作，实现业务规则（如权限校验、重复检查、级联操作）。

## LambdaQueryWrapper 条件构造器

组装 SQL 的 WHERE 条件：

```java
LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(User::getEmail, email);   // WHERE email = ?
wrapper.like(Article::getTitle, keyword);  // WHERE title LIKE %keyword%
```

### 常用方法

| 方法 | 对应 SQL |
|---|---|
| `eq(字段, 值)` | `column = ?` |
| `ne(字段, 值)` | `column != ?` |
| `like(字段, 值)` | `column LIKE %值%` |
| `gt(字段, 值)` | `column > ?` |
| `lt(字段, 值)` | `column < ?` |
| `ge(字段, 值)` | `column >= ?` |
| `le(字段, 值)` | `column <= ?` |
| `orderByDesc(字段)` | `ORDER BY column DESC` |
| `and(w -> w.eq(...))` | `AND (条件)` |
| `or(w -> w.eq(...))` | `OR (条件)` |

### LambdaQueryWrapper vs QueryWrapper

| | LambdaQueryWrapper | QueryWrapper |
|---|---|---|
| 写法 | `User::getEmail`（方法引用） | `"email"`（字符串） |
| 编译检查 | ✅ 字段写错编译就报错 | ❌ 运行时才抛异常 |
| 推荐 | ✅ 推荐 | ❌ 不推荐 |

## 权限校验

```java
Article article = this.getOptById(id)
        .orElseThrow(() -> new RuntimeException("文章不存在"));
if (article.getAuthorId() != UserContext.get()) {
    throw new RuntimeException("不能删除别人的文章");
}
this.removeById(id);
```

**模式：** 从 `UserContext` 获取当前用户 ID → 和资源的属主 ID 对比 → 不一致则拒绝。

## 级联操作

删除文章时同时删除该文章下的所有评论：

```java
LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(Comment::getArticleId, id);
commentMapper.delete(wrapper);   // 先删评论
this.removeById(id);             // 再删文章
```

## 事务 @Transactional

```java
@Transactional
public void someMethod() {
    // 方法内的所有数据库操作要么全部成功，要么全部回滚
}
```

在 `ServiceImpl` 中使用时，注意调用方式：**同一个类内方法直接调用事务不生效**（AOP 代理机制）。

## 异常处理

Service 层抛出 `RuntimeException`，由 Controller 层的 `@RestControllerAdvice` 全局捕获，统一返回 `Result.error()`。Service 不需要 try-catch，只管正常逻辑。
