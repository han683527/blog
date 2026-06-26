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
public void toggle(Long articleId) {
    Long userId = UserContext.get();
    LambdaQueryWrapper<ArticleLike> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(ArticleLike::getUserId, userId)
           .eq(ArticleLike::getArticleId, articleId);

    if (this.count(wrapper) > 0) {
        this.remove(wrapper);   // 已点赞 → 取消
    } else {
        ArticleLike like = new ArticleLike();
        like.setUserId(userId);
        like.setArticleId(articleId);
        notificationService.createNotification(userId, articleId, "LIKE");
        this.save(like);        // 未点赞 → 添加
    }
}
```

**为什么不用单独的方法区分点赞和取消？** 前端只需要一个按钮点来点去，后端判断状态，接口更少。

### 提取为独立 Service

点赞和收藏最初在 `ArticleServiceImpl` 里，后来各自提取成独立的 Service：

| | Service | Mapper | 职责 |
|---|---|---|---|
| 点赞 | `LikeServiceImpl` | `ArticleLikeMapper` | toggle / getLikeCount / isLiked / getMyLike / pageMyLike |
| 收藏 | `CollectServiceImpl` | `ArticleCollectMapper` | toggle / getCollectCount / isCollect / getMyCollect / pageMyCollect |

提取原因：
1. **单一职责** — `ArticleServiceImpl` 只处理文章 CRUD，点赞/收藏是独立的业务
2. **依赖简化** — 各自只需要自己的 Mapper + `NotificationService`，不需要 `ArticleMapper`
3. **复用** — `getMyLike` / `pageMyLike` 等操作在 Controller 层直接暴露，不用绕 ArticleService

## 收藏列表（一对多查询）

在 `CollectServiceImpl` 中：

```java
// 查当前用户收藏的文章
List<ArticleCollect> collects = this.list(
        new LambdaQueryWrapper<ArticleCollect>()
                .eq(ArticleCollect::getUserId, userId)
                .orderByDesc(ArticleCollect::getCreateTime));
List<Long> articleIds = collects.stream()
        .map(ArticleCollect::getArticleId).collect(Collectors.toList());

// 用查到的 ID 列表去查文章
wrapper.in(Article::getId, articleIds);
Page<Article> p = articleMapper.selectPage(new Page<>(...), wrapper);
```

这是一个**先查中间表 → 再查主表**的查询模式，跟按标签查文章的思路一样。

### 批量查当前用户操作状态

分页查完后，需要知道当前用户对哪些文章点过赞/收过藏——通过 `Set<Long>` 批量查：

```java
Set<Long> likedSet = likeService.getMyLike(articleIds, userId);
Set<Long> collectSet = collectService.getMyCollect(articleIds, userId);

for (ArticleResponse resp : list) {
    resp.setIsLike(likedSet.contains(resp.getId()));
    resp.setIsCollect(collectSet.contains(resp.getId()));
}
```

现在 `articleQueryService.enrich()` 接受了 `userId` 参数后，批量查时会一并设置 `isLike`/`isCollect`，Controller 不再需要额外处理。

## 当前用户操作状态

**单品查询**（文章详情页）直接调 Service 方法：
```java
boolean isLiked = likeService.isLiked(articleId, userId);
boolean isCollect = collectService.isCollect(articleId, userId);
```

**批量查询**（列表页）由 `enrich()` 内部批量查完后设置到每个 `ArticleResponse`。

```java
// enrich() 内部逻辑
Set<Long> likedIds = articleLikeMapper.selectList(
        eq(ArticleLike::getUserId, userId).in(ArticleLike::getArticleId, articleIds))
        .stream().map(ArticleLike::getArticleId).collect(Collectors.toSet());
// 在循环中设置
response.setIsLike(likedIds.contains(response.getId()));
```

为什么 Controller 不补这个字段？—— `ArticleQueryService` 持有 Mapper 直接查，不走 Service，避免循环依赖。

## 通知系统

操作（点赞/收藏/评论）时，如果操作者不是文章作者，创建一条通知。

```java
public void createNotification(Long actorId, Long articleId, String type) {
    Article article = articleService.getById(articleId);

    // 自己操作自己不通知
    if (article.getAuthorId().equals(actorId)) return;

    Notification notification = new Notification();
    notification.setUserId(article.getAuthorId());  // 接收人 = 文章作者
    notification.setActorId(actorId);                // 操作人
    notification.setArticleId(articleId);
    notification.setType(type);
    this.save(notification);
}
```

**调用时机：** 在 Toggle 的 insert 分支里调用，delete 分支不调用（取消操作不通知）。

```java
// 点赞时（LikeServiceImpl 中）
notificationService.createNotification(userId, articleId, "LIKE");
this.save(articleLike);

// 收藏时（CollectServiceImpl 中）
notificationService.createNotification(userId, articleId, "COLLECT");
this.save(articleCollect);
```

**注意：** 现在通知在各自的 Service 中触发，不再是 `ArticleServiceImpl` 负责。解耦后每个 Service 自己管理自己的通知。

### 通知的其他操作

| 方法 | 作用 |
|---|---|
| `pageNotifications(pageRequest)` | 当前用户的通知列表（分页） |
| `markAsRead(id)` | 标记单条已读 |
| `markAllAsRead()` | 全部标记已读 |
| `getUnreadCount()` | 未读通知数 |

全部已读通过 `update(notification, wrapper)` 实现 —— 不用循环逐条 update，一条 SQL 搞定：

```java
public void markAllAsRead() {
    LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(Notification::getUserId, UserContext.get())
           .eq(Notification::isRead, false);
    Notification notification = new Notification();
    notification.setRead(true);
    this.update(notification, wrapper);  // UPDATE ... WHERE user_id=? AND is_read=false
}
```

`ServiceImpl.update(entity, wrapper)` = `UPDATE 表 SET ... WHERE 条件`，批量更新不需要逐条操作。

## 服务拆分（分离关注点）

随着功能增多，`ArticleServiceImpl` 不断膨胀。以下是拆分历程：

### 拆前：ArticleServiceImpl 职责过重

```
ArticleServiceImpl
├── 文章 CRUD
├── 点赞 toggle / 计数 / 列表
├── 收藏 toggle / 计数 / 列表
├── 批量查标签
├── 批量查评论数
└── 缓存处理
```

### 拆后：职责分明

```
ArticleServiceImpl           → 文章 CRUD + 缓存
LikeServiceImpl              → 点赞相关操作
CollectServiceImpl           → 收藏相关操作
ArticleQueryService(@Component) → 批量查标签/点赞数/收藏数/评论数
```

### 判断标准

什么功能该抽离成独立 Service？

| 条件 | 例子 | 结论 |
|---|---|---|
| 有自己的数据表 | ArticleLike → LikeService | ✅ 可以抽 |
| 多个 Service 共用逻辑 | enrich() 被 4 个方法调用 | ✅ 可以抽 |
| 只用到 Mapper 基本方法 | selectCount + insert + delete | ✅ 适合抽 |
| 涉及复杂事务和业务规则 | 文章创建（分类校验 + 标签写入） | ⚠️ 保持内聚 |

### 依赖方向

```
Controller → ArticleService → LikeService / CollectService / ArticleQueryService
          → LikeService (direct)
          → CollectService (direct)
```

Controller 可以直调 `LikeService` / `CollectService`，不需要绕 `ArticleService`。这样 `ArticleService` 的接口保持精简，新增点赞相关 API 也不用改 `ArticleService`。



## 消除重复代码（服务抽取）

当多个方法有相同的「查标签 → 查点赞 → 查收藏 → 设值」逻辑时，逐步抽离：

### 第 1 步：私有方法

在 `ArticleServiceImpl` 中抽成私有方法，4 个分页方法各减 25 行。

### 第 2 步：独立 @Component

进一步抽成单独的 `ArticleQueryService`，用 `@Component` 注册：

```java
@Component
@RequiredArgsConstructor
public class ArticleQueryService {
    private final ArticleTagMapper articleTagMapper;
    private final CommentMapper commentMapper;
    private final ArticleLikeMapper articleLikeMapper;
    private final ArticleCollectMapper articleCollectMapper;

    public void enrich(List<ArticleResponse> list, List<Long> articleIds, Long userId) {
        if (articleIds == null || articleIds.isEmpty()) return;

        // 一次查全部标签
        List<ArticleTag> allTags = articleTagMapper.selectList(
                new LambdaQueryWrapper<ArticleTag>().in(ArticleTag::getArticleId, articleIds));
        Map<Long, List<Long>> tagMap = allTags.stream()
                .collect(Collectors.groupingBy(ArticleTag::getArticleId,
                        Collectors.mapping(ArticleTag::getTagId, Collectors.toList())));

        // 点赞数、收藏数、评论数 — 都是同样的模式
        Map<Long, Long> likeCountMap = articleLikeMapper.selectList(...)
                .stream().collect(Collectors.groupingBy(ArticleLike::getArticleId, Collectors.counting()));
        // ...

        for (ArticleResponse response : list) {
            response.setTags(tagMap.getOrDefault(response.getId(), List.of()));
            response.setLikeCount(likeCountMap.getOrDefault(response.getId(), 0L));
            response.setCollectCount(collectCountMap.getOrDefault(response.getId(), 0L));
            response.setCommentCount(commentCountMap.getOrDefault(response.getId(), 0L));
        }
    }
}
```

**好处：** `ArticleServiceImpl` 不再持有 4 个 Mapper，调用处变成一行：

```java
articleQueryService.enrich(list, articleIdList, UserContext.get());
```

`enrich()` 现在也设置 `isLike`/`isCollect`（需要当前用户 ID），避免了列表返回时这两个字段为 null 的问题。因为 `ArticleQueryService` 只持有 Mapper，不依赖其他 Service，不会循环依赖。

坏处是每个方法的参数检查（`null/empty`）和 Service 里的单品查询（`getArticleById`）仍然保留内联代码，因为单品走的查数据库和缓存逻辑，不走批量查。

## IN () 空列表 SQL 错误

当用户没有点赞/收藏时，`articleIds` 是空列表，MyBatis-Plus 生成 `WHERE id IN ()` 导致 MySQL 语法错误。

**修复：** 在查询前提前返回空分页：

```java
if (articleIds.isEmpty()) {
    PageResponse<ArticleResponse> response = new PageResponse<>();
    response.setTotal(0L);
    response.setList(List.of());
    return response;
}
```

所有「先查中间表→再查主表」的分页方法都要加这个守卫（`pageMyLike`、`pageMyCollect`）。

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
