# ==============================================
# OA System - Docker 多阶段构建
# ==============================================

# ---- 阶段1: Maven 构建 ----
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
# 预下载依赖（利用 Docker 缓存层）
RUN mvn dependency:go-offline -B -q
COPY src ./src
RUN mvn package -DskipTests -q

# ---- 阶段2: 运行时镜像 ----
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# 创建非 root 用户
RUN addgroup -S oa && adduser -S oa -G oa

# 创建数据目录
RUN mkdir -p /app/data /app/uploads /app/logs && \
    chown -R oa:oa /app

# 复制 JAR
COPY --from=build /app/target/oa-system.jar /app/oa-system.jar

# 复制 SQL 初始化脚本（首次部署时手动导入）
COPY sql/schema-mysql.sql /app/sql/schema-mysql.sql

USER oa
EXPOSE 8080

# 默认使用 prod 配置（MySQL），可通过环境变量覆盖
ENTRYPOINT ["java", "-jar", "/app/oa-system.jar"]
CMD ["--spring.profiles.active=prod"]
