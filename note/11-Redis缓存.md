# Redis 缓存

## 为什么用缓存

数据库数据存在磁盘上，每次查询都有 I/O 开销。Redis 存在内存中，读写速度比磁盘快几个数量级。

流程：先查 Redis → 有就直接返回（缓存命中）→ 没有就去数据库查 → 查到后写入 Redis 并设置 TTL。

## 缓存模式：Cache-Aside

本项目采用 Cache-Aside 模式（旁路缓存）：

**读：**
1. 查缓存 → 有则直接返回
2. 缓存没有 → 查数据库
3. 数据库查到 → 写入缓存（设 TTL），返回

**写：**
1. 更新数据库
2. 删除缓存（而不是更新缓存）

为什么不更新缓存而删除？—— 更新缓存涉及复杂的数据组装，删除只需要一行，下次读的时候自动回填。

## 缓存穿透

请求一个**不存在的数据**（如查 ID = 99999 的文章），缓存里没有，每次都打到数据库，高并发下数据库压力剧增。

**解决方案：空值缓存**

查数据库发现不存在时，在 Redis 里写一个特殊标记（如 `"NULL"`）并设置短 TTL（如 1 分钟），后续相同请求直接拦截在缓存层。

```java
Article article = this.getById(id);
if(article == null){
    redisTemplate.opsForValue().set(key,"NULL",1, TimeUnit.MINUTES);
    throw new NotFoundException("文章不存在");
}
```

读取时先判断是否为空值标记：

```java
String cached = redisTemplate.opsForValue().get(key);
if(cached != null){
    if("NULL".equals(cached)){
        throw new NotFoundException("文章不存在");
    }
    return JSONUtil.toBean(cached, Article.class);
}
```

## TTL（过期时间）

TTL 是缓存的安全兜底机制：
- **主动删除**（update/delete 时删缓存）是主要手段
- **TTL 过期**是保障：防止删缓存失败（代码 bug、异常等）导致的数据永久不一致
- 项目中的 TTL 设置：文章 10 分钟，评论 60 分钟，空值缓存 1 分钟

## 缓存雪崩（了解）

大量缓存同时过期，请求全部打到数据库。解决：TTL 加随机偏移量，避免同时过期。

## 缓存击穿（了解）

一个热点 key 过期瞬间，大量并发请求同时打到数据库。解决：互斥锁（只让一个线程去查 DB 回填缓存）。

## 依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

## 配置

```yaml
spring:
  data:
    redis:
      host: 172.27.212.128
      port: 6379
```

## 缓存与字段变更的问题

当实体新增字段（如 `viewCount`）时，**旧缓存里的 JSON 没有这个字段**，反序列化后为 null，不是数据库的默认值。

```java
// 旧缓存的 JSON（没有 viewCount）
{"id":22, "title":"xxx", ...}

// 反序列化后 viewCount = null
Article article = JSONUtil.toBean(cached, Article.class);
```

**解决：** 使用前加 null 判断

```java
article.setViewCount(article.getViewCount() == null ? 0 : article.getViewCount() + 1);
```

或者**清掉旧缓存**让它们重新从数据库加载。TTL 到期后也会自动刷新。

## 常用 Redis 命令

```bash
redis-cli ping           # 测试连接，返回 PONG
redis-cli get article:1  # 获取某个 key 的值
redis-cli keys article:* # 匹配所有 article 前缀的 key
redis-cli ttl article:1  # 查看 key 剩余过期时间
redis-cli del article:1  # 删除 key
redis-cli flushall       # 清空所有数据（仅开发环境）
```
