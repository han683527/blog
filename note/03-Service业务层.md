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

### 条件查询（可选参数）

当参数可能为 null 时，加 `if` 判断来决定是否拼入 WHERE 条件：

```java
LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
if(categoryId != null){
    wrapper.eq(Article::getCategoryId, categoryId);
}
Page<Article> p = this.page(new Page<>(page, size), wrapper);
```

`categoryId` 为 null 时不加条件查全部，有值时才筛选。避免用字符串拼接 SQL。`

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

## 多条件组合查询（pageArticle）

```java
public PageResponse<ArticleResponse> pageArticle(ArticleSearchRequest request) {
    LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();

    // 1. 模糊查找（keyword）
    if (keyword != null && !keyword.isEmpty()) {
        wrapper.like(Article::getTitle, keyword);
    }

    // 2. 按分类筛选（categoryId）
    if (categoryId != null) {
        wrapper.eq(Article::getCategoryId, categoryId);
    }

    // 3. 按标签筛选（tagIds）
    if (tagIds != null && !tagIds.isEmpty()) {
        // 两步查询：先查中间表拿到文章 ID，再查文章
        List<ArticleTag> articleTags = articleTagMapper.selectList(
                new LambdaQueryWrapper<ArticleTag>().in(ArticleTag::getTagId, tagIds));
        List<Long> articleIds = articleTags.stream()
                .map(ArticleTag::getArticleId).distinct().collect(Collectors.toList());
        wrapper.in(Article::getId, articleIds);
    }

    Page<Article> p = this.page(Page.of(page, size), wrapper);
    ...
}
```

**多个条件同时满足**（AND 关系）—— 后端只拼条件，SQL 端 WHERE 条件叠加。

**标签的两步查询：** 因为文章和标签隔着一张中间表，不能直接在文章表上 WHERE。拆成：
```
SELECT article_id FROM Article_Tag WHERE tag_id IN (1,2)   → [1, 3, 5]
SELECT * FROM Article WHERE id IN (1, 3, 5)                → 最终结果
```

## 批量查标签（为文章列表补全标签信息）

分页查完文章后，每篇文章有哪些标签需要从中间表查出来。不能用 N+1 循环，而是**批量查**：

```java
List<Long> articleIdList = p.getRecords().stream()
        .map(Article::getId).collect(Collectors.toList());

if (!articleIdList.isEmpty()) {
    // 一次性查出所有文章对应的中间表记录
    List<ArticleTag> allTags = articleTagMapper.selectList(
            new LambdaQueryWrapper<ArticleTag>().in(ArticleTag::getArticleId, articleIdList));

    // 按文章 ID 分组：{ articleId: [tagId1, tagId2], ... }
    Map<Long, List<Long>> tagMap = allTags.stream()
            .collect(Collectors.groupingBy(
                    ArticleTag::getArticleId,
                    Collectors.mapping(ArticleTag::getTagId, Collectors.toList())));

    // 设置到每个 response 中
    for (ArticleResponse response : list) {
        response.setTags(tagMap.getOrDefault(response.getId(), List.of()));
    }
}
```

**为什么不用循环？** N 篇文章查 N 次 SQL（N+1 问题），批量查只用 2 次 SQL 就解决。

### 同样的模式用于点赞数 / 收藏数

```java
// 查询所有文章的点赞数
Map<Long, Long> likeCountMap = articleLikeMapper.selectList(
        new LambdaQueryWrapper<ArticleLike>()
                .in(ArticleLike::getArticleId, articleIdList))
        .stream().collect(Collectors.groupingBy(
                ArticleLike::getArticleId,
                Collectors.counting()));

// 查询所有文章的收藏数（代码一模一样，换个表名）
Map<Long, Long> collectCountMap = articleCollectMapper.selectList(...)
        .stream().collect(Collectors.groupingBy(
                ArticleCollect::getArticleId,
                Collectors.counting()));

// 设置到 response
for (ArticleResponse resp : list) {
    resp.setLikeCount(likeCountMap.getOrDefault(resp.getId(), 0L));
    resp.setCollectCount(collectCountMap.getOrDefault(resp.getId(), 0L));
}
```

`groupingBy + counting` = 模拟 SQL 的 `GROUP BY ... COUNT(*)`，在 Java 内存中完成聚合，避免 N+1 次 `selectCount`。

## 切换操作（Toggle）

点赞和收藏是**开关操作**：同一个请求，有则取消，无则添加。

```java
public void likeArticleById(Long id) {
    Long userId = UserContext.get();
    LambdaQueryWrapper<ArticleLike> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(ArticleLike::getUserId, userId)
           .eq(ArticleLike::getArticleId, id);

    if (articleLikeMapper.selectCount(wrapper) > 0) {
        articleLikeMapper.delete(wrapper);   // 已点赞 → 取消
    } else {
        ArticleLike like = new ArticleLike();
        like.setUserId(userId);
        like.setArticleId(id);
        articleLikeMapper.insert(like);      // 未点赞 → 添加
    }
}
```

**为什么不用单独的方法区分点赞和取消？** 前端只需要一个按钮点来点去，后端判断状态，接口更少。

## 收藏列表（一对多查询）

```java
// 查当前用户收藏的文章
List<ArticleCollect> collects = articleCollectMapper.selectList(
        new LambdaQueryWrapper<ArticleCollect>()
                .eq(ArticleCollect::getUserId, userId)
                .orderByDesc(ArticleCollect::getCreateTime));
List<Long> articleIds = collects.stream()
        .map(ArticleCollect::getArticleId).collect(Collectors.toList());

// 用查到的 ID 列表去查文章
wrapper.in(Article::getId, articleIds);
Page<Article> p = this.page(new Page<>(page, size), wrapper);
```

这是一个**先查中间表 → 再查主表**的查询模式，跟按标签查文章的思路一样。

## 涉及的 Stream API

```java
// 转换：List<Article> → List<Long>（id 列表）
p.getRecords().stream().map(Article::getId).collect(Collectors.toList())

// 去重
stream().distinct().collect(Collectors.toList())

// 分组：{ articleId: [tagId1, tagId2] }
stream().collect(Collectors.groupingBy(
        ArticleTag::getArticleId,                              // 按哪个字段分组
        Collectors.mapping(ArticleTag::getTagId,               // 取每个元素的哪个字段
                Collectors.toList())))                         // 收集成什么类型

// 缺省值（避免空指针）
tagMap.getOrDefault(response.getId(), List.of())
//      ↑ 有这个 key 返回值    ↑ 没有返空列表
```

`groupingBy` 相当于 SQL 的 `GROUP BY`。组合 `Collectors.mapping` 可以在分组的同时转换字段类型。

## 异常处理

Service 层抛出 `RuntimeException`，由 Controller 层的 `@RestControllerAdvice` 全局捕获，统一返回 `Result.error()`。Service 不需要 try-catch，只管正常逻辑。
