# OA System

本仓库为 OA 办公自动化系统后端代码。

快速说明：
- 运行（开发环境，H2）：

```powershell
cd D:\CLion\oa-system
D:\CLion\tools\apache-maven-3.9.16\bin\mvn spring-boot:run
```

- API 根路径：`http://localhost:8080/oa`
- API 文档（Knife4j / Swagger）：`http://localhost:8080/oa/doc.html`

开发迭代记录：

- DEV-01: 启用 JWT 认证基础集成（Security 配置更新、JWT 过滤器接入）。

详细开发文档请见 `doc/` 目录。

