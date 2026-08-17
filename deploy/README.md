# 生产部署（Docker）

博客生产环境部署。目标服务器：阿里云 ECS（Alibaba Cloud Linux 4，2G 内存）。

## 目录结构

```
deploy/
├── docker-compose.yml          # 5 个服务编排:mysql redis es backend nginx
├── .env.example                # 敏感配置模板(复制为 .env 填写)
├── backend/
│   ├── Dockerfile              # 后端镜像
│   ├── application-prod.yml    # 生产配置(挂载进容器)
│   └── app.jar                 # ⚠ 本地打包后上传,git 忽略
├── es/
│   ├── Dockerfile              # ES + IK 分词镜像
│   ├── analysis-ik/            # IK 插件(从本地容器拷出,git 忽略)
│   └── config-analysis-ik/     # IK 词典(从本地容器拷出,git 忽略)
└── nginx/
    ├── Dockerfile              # 前端镜像
    ├── nginx.conf              # 反代 + SSE 支持
    └── dist/                   # ⚠ 本地 npm run build 后上传,git 忽略
```

## 一、本地准备

1. 打包后端：`mvnw clean package -DskipTests`
2. 把 `user/target/user-0.0.1-SNAPSHOT.jar` 复制为 `deploy/backend/app.jar`
3. 打包前端：`cd frontend && npm run build`
4. 把 `frontend/dist/` 整个复制为 `deploy/nginx/dist/`

> IK 插件（`deploy/es/analysis-ik`、`config-analysis-ik`）已从本地正常运行的容器拷出，无需重新下载。

## 二、上传到服务器

把 `deploy/` 整个目录传到服务器（root 用户，目标 /opt/blog）：

```bash
scp -r deploy root@120.55.60.9:/opt/blog/
```

Windows 也可以用 WinSCP 拖拽上传。

## 三、服务器一次性配置

```bash
# 1. 配置 .env(填数据库密码 / 邮箱 / JWT 密钥)
cd /opt/blog/deploy
cp .env.example .env
vi .env

# 2. ES 需要的系统参数(要永久生效,写入 sysctl.conf)
sysctl -w vm.max_map_count=262144
echo 'vm.max_map_count=262144' >> /etc/sysctl.conf

# 3. 如果 .env 里 JWT_SECRET 想用随机值,服务器上执行生成
openssl rand -hex 32

# 4. 2G 内存强烈建议加 4G swap,防止内存不够 OOM
fallocate -l 4G /swapfile && chmod 600 /swapfile
mkswap /swapfile && swapon /swapfile
echo '/swapfile none swap sw 0 0' >> /etc/fstab
```

## 四、启动

```bash
cd /opt/blog/deploy
docker compose up -d --build
```

- 第一次会拉 mysql / redis / nginx / temurin / ES 基础镜像并构建 ES / backend / nginx，需几分钟
- 查看状态：`docker compose ps`
- 看后端日志：`docker compose logs -f backend`
- 首次启动 Flyway 自动建表；ES 索引在首次发布文章时自动创建

## 五、验证

- 浏览器访问 `http://120.55.60.9/` 应看到首页
- 发布文章 → 搜索能搜到（IK 中文分词 + 高亮）
- 上传图片 → 文章内图片能显示
- 登录后收到评论 / 点赞时，导航栏角标实时刷新（SSE）

## 六、阿里云安全组

**只放行 80（http）和 22（ssh）**。3306 / 6379 / 9200 / 8080 已在 compose 里绑定到 127.0.0.1，外网无法访问。

## 后续更新流程

1. 本地重新打包 jar / dist（见"一、本地准备"）
2. scp 覆盖 `deploy/backend/app.jar`、`deploy/nginx/dist/`
3. 服务器上 `cd /opt/blog/deploy && docker compose up -d --build`
4. 数据库结构改动：新增 Flyway `V18+.sql` 放进去，重启后端自动执行（不要改旧的 V1~V17）

## 备份数据库

```bash
docker exec blog-mysql mysqldump -uroot -p"$(grep MYSQL_PASSWORD .env | cut -d= -f2)" blog > blog_$(date +%F).sql
```

## 常见问题

- **ES 起不来**：`docker compose logs es`，看是否报 `max_map_count` 或内存不足
- **后端反复重启**：多半在等 ES 就绪，`docker compose logs -f backend` 看日志
- **镜像拉不下来**（国内网络）：给 Docker 配置阿里云镜像加速器，然后重试
