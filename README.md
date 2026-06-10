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
 - DEV-02: 添加 .gitignore（忽略 target、IDE 配置、H2 本地数据等），更新文档与迭代记录。
 - DEV-03: 添加根路径健康接口（GET /），并调整 Security 配置允许根路径无鉴权，方便访问应用根地址。
	 - DEV-04: 实现角色管理基础 CRUD、菜单树接口、用户-角色分配，并更新文档与 DB 脚本。
	 - DEV-05: 实现权限注入（UserDetailsService），在 JWT 认证中加载用户权限并完善 `SecurityUtils`。

详细开发文档请见 `doc/` 目录。

