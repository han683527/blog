# 前端镜像:nginx 托管打包产物 + 反向代理后端
# 部署前先把本地 npm run build 的产物上传到 deploy/nginx/dist/(主站)
# 和 deploy/nginx/admin-dist/(管理后台,8092 端口)
FROM nginx:1.27-alpine

COPY dist/ /usr/share/nginx/html/
COPY admin-dist/ /usr/share/nginx/html-admin/
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80 8092
