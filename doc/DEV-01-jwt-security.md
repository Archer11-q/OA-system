# DEV-01：JWT 认证基础集成

目的与范围

- 启用后端 Spring Security 的 JWT 认证基础，确保非公开接口需要通过有效 JWT 访问。
- 将已有的 `JwtAuthenticationFilter` 接入 Spring Security 过滤链，并把默认的 `anyRequest().permitAll()` 调整为 `anyRequest().authenticated()`。
- 提供临时的 SecurityContext 填充（以用户名为 principal，权限为空），后续迭代会替换为基于 `UserDetailsService` 的权限加载。

已修改文件

- `src/main/java/com/oasystem/config/SecurityConfig.java`
  - 将 `SecurityFilterChain` 方法注入 `JwtAuthenticationFilter`，并在过滤链中添加 `addFilterBefore(...)`。
  - 将 `.anyRequest().permitAll()` 改为 `.anyRequest().authenticated()`，保留 `/auth/**`、Swagger 与 H2 控制台等公开路径。

- `src/main/java/com/oasystem/security/JwtAuthenticationFilter.java`
  - 在验证通过后，构造 `UsernamePasswordAuthenticationToken` 并设置到 `SecurityContextHolder`，使受保护接口能够识别已认证的主体。

- `README.md`（根目录）
  - 新增开发迭代记录，标注 DEV-01 状态。

变更说明与影响范围

- 此次迭代将开启对非公开接口的认证检查。已有公开接口（如登录 `/auth/login`、Swagger 文档、H2 控制台）仍保持放行。
- 由于当前 `JwtAuthenticationFilter` 的实现为临时实现（未加载用户权限），权限控制（基于角色/菜单）尚未生效；但后续迭代会完善 `UserDetailsService` 和权限数据加载。

测试建议

1. 启动应用（开发环境，H2）

```powershell
cd D:\CLion\oa-system
D:\CLion\tools\apache-maven-3.9.16\bin\mvn spring-boot:run
```

2. 测试公开接口（无需 Token）
- 登录接口：POST `http://localhost:8080/oa/auth/login`（按项目原有测试账户执行）
- Swagger：访问 `http://localhost:8080/oa/doc.html`

3. 测试受保护接口（需 Token）
- 先登录获取 `Authorization: Bearer <token>`，然后访问任一需认证的接口（例如 `GET /oa/system/user/page`）应返回 200（如果 token 有效）或 401（如果 token 无效）。

后续迭代建议（优先级）

- DEV-02：实现 `UserDetailsService`，在 `JwtAuthenticationFilter` 中加载用户详细信息并填充权限（基于 `sys_user` / `sys_user_role` / `sys_role` / `sys_role_menu`）。
- DEV-03：实现角色管理（CRUD）与菜单权限分配，完成 RBAC 的数据库端实现。



