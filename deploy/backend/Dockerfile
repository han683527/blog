# 后端镜像:只需要 JDK17 运行时 + 打好的 jar
# 部署前先把本地打包的 jar 上传到 deploy/backend/ 并命名为 app.jar
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY app.jar /app/app.jar
# 生产配置会挂载到 /app/application-prod.yml(见 docker-compose.yml),覆盖 jar 内置配置

EXPOSE 8080

# 2G 服务器内存紧张,限制堆内存 256m 防止 OOM
ENTRYPOINT ["java", "-Xmx256m", "-jar", "/app/app.jar", "--spring.profiles.active=prod"]
