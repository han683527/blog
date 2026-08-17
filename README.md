# 半笺云纸

个人博客平台（用户端 + 管理后台）。Spring Boot 后端，Vue 3 前端，可用 Docker Compose 在服务器上部署。

在线预览：http://120.55.60.9/

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Java 17、Spring Boot 3.5、MyBatis-Plus、Flyway |
| 存储 | MySQL 8、Redis、Elasticsearch 7（IK 中文分词） |
| 鉴权 | JWT access + refresh、`TokenInterceptor` |
| 前端 | Vue 3、Vite、Element Plus、Vue Router |
| 部署 | Docker Compose、Nginx |

## 功能

- 注册 / 登录（邮箱验证码）、资料与头像
- 文章发布、草稿、Markdown、图片上传、浏览量
- 评论、点赞、收藏；SSE 实时通知
- 分类 / 标签筛选
- Elasticsearch + IK 全文搜索与关键词高亮
- 基于浏览 / 点赞 / 收藏 / 评论加权的内容偏好召回；无行为用户回退热门
- 管理后台：文章、评论、用户、标签、分类

## 仓库结构

```
blog
├── common/              公共模块：JWT、拦截器、统一响应、异常
├── user/                后端启动模块 BlogApplication
├── frontend/            用户端（Vite 开发端口 5173）
├── frontend-admin/      管理后台（端口 5174）
├── docker-compose.yml   本地 MySQL / Redis / ES / Kibana
└── deploy/              生产编排（Nginx + 后端 + 依赖）
```

## 本地运行

### 1. 依赖

JDK 17、Maven（或用仓库里的 `mvnw`）、Node.js 18+、Docker。

```bash
docker compose up -d mysql redis elasticsearch
```

### 2. 后端

把 `user/src/main/resources/application.yml` 复制为 `application-dev.yml`（该文件已被 git 忽略），填入本机 MySQL 密码、Redis、ES、邮箱授权码、JWT secret。

```bash
mvnw spring-boot:run -pl user
```

- 接口：http://localhost:8080
- Swagger：http://localhost:8080/swagger-ui/

启动时 Flyway 会自动建表。

### 3. 前端

```bash
cd frontend && npm install && npm run dev
cd frontend-admin && npm install && npm run dev
```

用户端默认把 `/article`、`/auth` 等代理到 `8080`。

## 生产部署

见 [deploy/README.md](deploy/README.md)。敏感配置只放在 `deploy/.env`，不要提交。

## 配置注意

- 不要把 `application-dev.yml`、QQ 邮箱授权码、JWT 密钥推到 GitHub
- 示例配置在 `user/src/main/resources/application.yml` 和 `deploy/.env.example`
