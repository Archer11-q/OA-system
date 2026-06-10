# OA System — 全栈（Vue 3 + Spring Boot）

本仓库包含 OA 办公自动化系统的后端（Spring Boot + MyBatis-Plus + JWT）与前端（Vue 3 + Element Plus）源码及文档。

## 快速运行

### 后端（开发环境，内置 H2）

```powershell
cd D:\CLion\oa-system
D:\CLion\tools\apache-maven-3.9.16\bin\mvn spring-boot:run
```

访问：
- 应用根路径: http://localhost:8080/oa
- API 文档（Knife4j/Swagger）： http://localhost:8080/oa/doc.html
- H2 控制台： http://localhost:8080/oa/h2-console （JDBC URL: jdbc:h2:file:./data/oa-system，用户: sa，密码: 空）

### 前端（Vue 3 + Element Plus）

```powershell
cd D:\CLion\oa-system\frontend
npm install
npm run dev
```

访问：http://localhost:5173 （自动代理后端 localhost:8080，默认账号 admin/123456）

### Docker 一键部署

```powershell
cd D:\CLion\oa-system
docker-compose up -d
```

## 项目当前状态（与 `doc/DESIGN.md` 保持一致）

- 模块1 - 系统管理：✅ 用户/角色/菜单/部门 CRUD + RBAC 权限 + 数据权限（dataScope）+ 文件上传 + 数据看板
- 模块2 - 考勤：✅ 签到/签退（迟到早退判定）+ 月度汇总 + 每日明细 + 请假自动创建多级审批（DEV-22）
- 模块3 - 审批中心：✅ 多级审批引擎（DEPT_LEADER/ROLE/USER 三种审批人）+ 模板管理 + 撤回功能（DEV-26）+ 请假/报销状态回调
- 模块4 - 公告通知：✅ 列表/详情/发布/编辑/删除
- 模块5 - 日程管理：✅ CRUD + 日期范围查询 + 个人权限控制
- 模块6 - 报销管理：✅ CRUD + 统计 + 自动创建多级审批（DEV-25）
- 操作日志：✅ AOP 自动记录（@Log 注解）
- 前端：✅ Vue 3 + Element Plus + Router + Pinia（DEV-28/DEV-29）：登录/布局/数据看板(ECharts图表)/用户管理(CRUD)/角色管理(CRUD+菜单分配)/菜单管理(树形)/部门管理(树形)/考勤管理(签到签退+请假+统计)/审批中心(多Tab+时间线)/公告通知(CRUD)/日程管理(CRUD)/报销管理(CRUD+统计)
- 测试：✅ 22 单元测试（ApprovalServiceImpl / AttendanceServiceImpl / UserServiceImpl）
- 部署：✅ Dockerfile 多阶段构建 + docker-compose.yml（MySQL + App）

主要开发/迭代记录简要（最新在 `doc/DESIGN.md` 中）：

 - DEV-01..DEV-07：项目骨架、JWT+Security、角色/菜单/部门管理与权限
 - DEV-08：考勤模块基础（签到/签退/请假）
 - DEV-09：公告模块基础
 - DEV-10：审批中心骨架（模板/实例/记录）
 - DEV-11..DEV-14：RBAC 完善、dataScope、菜单安全增强
 - DEV-15：日程管理 CRUD
 - DEV-16：审批中心多级审批引擎完善
 - DEV-17：报销管理 CRUD
 - DEV-18：考勤统计增强（迟到/早退判定+月度报表）
 - DEV-19：操作日志 AOP（@Log注解+切面）
 - DEV-20：文件上传（通用+头像+静态资源映射）
 - DEV-21：数据看板 API（概览/趋势/分布）
 - DEV-22：请假申请与审批中心集成（自动创建审批实例+审批完成回调同步状态）
 - DEV-23：MySQL 生产环境配置（application-prod.yml + HikariCP 连接池 + 文件日志）
 - DEV-24：单元测试（JUnit 5 + Mockito 覆盖三大核心 Service，19 测试用例全部通过）
 - DEV-25：报销申请与审批中心集成（自动创建审批实例+审批完成回调同步状态）
 - DEV-26：审批撤回功能（申请人撤回审批中实例+同步业务状态）
 - DEV-27：Docker 部署（Dockerfile 多阶段构建 + docker-compose.yml MySQL+App）
 - DEV-28：前端项目初始化（Vue 3 + Element Plus + Router + Pinia + Axios，登录+布局+看板+用户管理）
 - DEV-29：前端业务页面全面完善（8个API模块+10个页面完善+ECharts图表集成）
 - HOTFIX：修复 DashboardController 第104行 int 基本类型 equals() 编译错误

Phase 1-5 全部完成。后端 6 大模块 + 审批流程闭环 + 前端框架就绪。

说明与注意：
- `doc/DESIGN.md` 是主设计与迭代记录文档，后续请以该文件为权威进度记录。README 只保留高层摘要与启动说明。
- 你打算把 `DESIGN.md` 给其他智能体使用时，我会保持该文件为单一真实来源（同步更新实现/未实现项），并把 README 保持为简短引用。

详细技术设计、接口说明和数据字典请查看 `doc/DESIGN.md`。

---

## 下一步可开展工作

| 优先级 | 方向 | 说明 |
|--------|------|------|
| 🟡 | 并行审批策略 | 同一级别多人审批，任一人同意即可 |
| 🟡 | 日程日历视图 | FullCalendar 组件对接，日/周/月视图 |
| 🟡 | 前端性能优化 | 路由懒加载、组件缓存、打包优化 |
| 🟡 | 接口文档完善 | 补充 Knife4j 文档注解细节 |
