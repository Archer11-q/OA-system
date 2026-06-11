# OA 办公自动化系统 — 项目设计文档

> **版本**: 1.0.0  
> **日期**: 2026-06-10  
> **技术栈**: Spring Boot 3.2 + MyBatis-Plus + MySQL/H2 + Spring Security + JWT  
> **目标**: 完整的企业级 OA 系统，含 6 大模块，可作为课程设计/毕业设计交付

> **迭代记录**：
>
>- DEV-01 (2026-06-10)：基础框架与 JWT 认证集成、用户管理 CRUD、H2 开发环境、API 文档（Knife4j）
>- DEV-02 (2026-06-10)：添加 `.gitignore` 忽略本地构建与 IDE 文件，更新项目文档（README、DESIGN）
>- DEV-03 (2026-06-10)：添加根路径健康接口 `GET /`（HomeController），并在 `SecurityConfig` 中允许根路径无鉴权，方便访问应用根地址查看运行状态。
>- DEV-04 (2026-06-10)：实现角色管理基础 CRUD、菜单树接口、用户-角色分配（Role/RoleMenu/UserRole），并更新文档与 DB 脚本。
>- DEV-05 (2026-06-10)：实现权限注入（UserDetailsService），在 JWT 认证中加载用户权限并填充 SecurityContext，完善 `SecurityUtils` 获取当前用户与权限（本次迭代完成）。
>- DEV-06 (2026-06-10)：实现菜单管理 CRUD（新增/删除/查询树）并在敏感接口添加方法级权限注解（@PreAuthorize），完善角色/用户管理的权限校验。
>- DEV-07 (2026-06-10)：实现部门管理 CRUD（新增/更新/删除/查询树），并为部门写操作添加管理员权限校验。
>- DEV-08 (2026-06-10)：实现考勤模块基础（签到/签退/按月查询/请假申请），包含实体、Mapper、Service 与 Controller。文档与 README 已同步更新。
>- DEV-09 (2026-06-10)：实现公告通知模块基础（公告列表/详情/发布/编辑/删除），并为发布/编辑/删除接口添加管理员权限校验。
>- DEV-10 (2026-06-10)：实现审批中心基础（审批模板/实例/记录）：发起审批、待审批/已审批/我的申请查询、审批操作与审批记录查询。
>- DEV-11 (2026-06-10)：RBAC 完善：为角色新增唯一编码校验，删除角色前检查是否被用户分配，禁止删除并抛出业务异常；更新 RoleService 实现。
>- DEV-12 (2026-06-10)：实现数据权限（dataScope）基础支持：在用户分页查询中根据当前用户角色的 dataScope 自动注入过滤条件（全部/本部门/仅本人，简化处理子部门）。
>- DEV-13 (2026-06-10)：完善菜单管理：新增菜单与更新接口加入 perms 唯一性校验，新增菜单更新接口，删除时清理角色-菜单关联。
>- DEV-14 (2026-06-10)：菜单删除安全性增强：禁止删除已被角色引用的菜单，要求先从角色中移除引用后再删除。
>- DEV-15 (2026-06-10)：实现日程管理模块 CRUD（ScheduleMapper/Service/Controller），支持按日期范围查询、新增/更新/删除日程，个人日程权限控制（仅创建人可修改/删除）。
>- DEV-16 (2026-06-10)：完善审批中心多级审批引擎：审批人配置（DEPT_LEADER/ROLE/USER三种类型）、审批人解析与快照、预创建审批记录、审批权限验证（用户只能审批自己负责的级别）、审批模板 CRUD（Admin权限管理）、统一使用 BusinessException 处理业务异常。
>- DEV-17 (2026-06-10)：实现报销管理模块 CRUD（ExpenseMapper/Service/Controller），支持提交报销申请/列表查询（按状态过滤）/修改/删除，个人权限控制（只能操作自己的申请且仅待审批状态可修改/删除），报销统计接口（按状态汇总数量和金额）。
>- DEV-18 (2026-06-10)：考勤统计增强：签到自动判定迟到（晚于9:00）、签退自动判定早退（早于18:00）、自动计算工作时长、月度考勤汇总（出勤/迟到/早退/缺勤天数+总工时）、每日考勤状态明细（含周末识别）。
>- DEV-19 (2026-06-10)：实现操作日志记录（AOP）：自定义 @Log 注解 + OperLogAspect 切面自动记录 Controller 关键操作（用户/模块/方法/参数/结果/IP/耗时/异常），操作日志分页查询与清理接口（Admin权限），已在全部模块的关键写操作添加 @Log 注解。
>- DEV-20 (2026-06-10)：实现文件上传功能：通用文件上传/头像上传接口（FileController），支持文件类型校验、按日期分目录存储、UUID 重命名防冲突，WebMvcConfig 配置静态资源映射使上传文件可通过 URL 直接访问。
>- DEV-21 (2026-06-10)：实现数据看板后端 API（DashboardController）：系统概览（用户/部门/角色/公告数量）、今日考勤统计、本月审批汇总、本月报销汇总、近7天考勤趋势、审批状态分布、报销类型分布，为前端 ECharts 图表提供数据。
>- DEV-22 (2026-06-10)：请假申请与审批中心集成：员工提交请假自动创建审批实例走多级审批流，审批完成后自动同步请假状态；appr_instance 表新增 business_type/business_id 通用业务关联字段；att_leave_request 表新增 approval_instance_id 关联审批实例；预设请假审批模板（部门负责人→管理员 2级审批）。
>- DEV-23 (2026-06-10)：MySQL 生产环境配置：新增 application-prod.yml（MySQL + HikariCP 连接池 + 文件日志），application-dev.yml 保持 H2 开发环境；.gitignore 补充 /uploads/ 忽略规则；更新部署文档。
>- DEV-24 (2026-06-10)：单元测试：JUnit 5 + Mockito 覆盖 ApprovalServiceImpl（多级审批引擎 12 用例）、AttendanceServiceImpl（请假审批集成 6 用例）、UserServiceImpl（登录/用户管理/密码重置 8 用例），全部 19 个测试通过。
>- DEV-25 (2026-06-10)：报销申请与审批中心集成：员工提交报销自动创建审批实例走多级审批流，审批完成后自动同步报销状态（复用 DEV-22 business_type/business_id 回调机制）；exp_request 表新增 approval_instance_id；预设报销审批模板（部门负责人→管理员 2级审批）。
>- DEV-26 (2026-06-10)：审批撤回功能：申请人可撤回自己审批中的实例（POST /approval/{id}/cancel），撤回后同步更新关联业务记录状态；新增 3 个撤回测试用例，累计 22 测试全部通过。
>- HOTFIX (2026-06-10)：修复 DashboardController.java 第104行编译错误 — `Constants.APPROVAL_APPROVED`（int基本类型）无法作为 `.equals()` 的 receiver，改用 `e.getStatus() != null && e.getStatus() ==` 判空后比较。
>- DEV-27 (2026-06-10)：Docker 部署：Dockerfile（多阶段构建：Maven 编译 + JRE 运行镜像）+ docker-compose.yml（MySQL 8.0 + OA 应用，健康检查+数据卷持久化），MySQL 初始化脚本自动导入。
>- DEV-28 (2026-06-10)：前端项目初始化（Vue 3 + Vite + Element Plus + Vue Router + Pinia + Axios）：登录页（JWT 认证+路由守卫）、主布局（侧边栏菜单+折叠+头部导航+用户下拉）、数据看板（概览卡片+模块入口）、用户管理列表（分页查询+表格）；Axios 封装（Token 注入+401 拦截）；8 个业务模块路由/视图骨架已建立。
>- DEV-29 (2026-06-10)：前端业务页面全面完善：新建 8 个 API 模块（role/menu/dept/attendance/approval/notice/schedule/expense）；完善 10 个业务页面——用户管理（CRUD 对话框+搜索+删除确认）、角色管理（CRUD+菜单权限分配树）、菜单管理（树形表格+类型联动表单）、部门管理（树形表格+级联选择）、公告通知（列表+发布/编辑+权限控制）、日程管理（日期范围筛选+CRUD）、报销管理（状态筛选+统计面板）、考勤管理（签到签退+月度统计+请假申请）、审批中心（多Tab：待审批/我的申请/已审批/模板管理+审批时间线）；数据看板集成 ECharts 图表（考勤趋势折线图+审批分布饼图+报销类型柱状图）。
>- HOTFIX (2026-06-11)：修复认证流程四大连锁问题 — 1) 创建缺失的 MyBatis XML 映射文件（RoleMapper.xml / MenuMapper.xml / AttendanceMapper.xml），`selectRolesByUserId` 和 `selectMenusByUserId` 方法只有接口声明无 SQL 实现，导致 `getCurrentUserInfo()` 抛出 BindingException → 500；2) 修正 SecurityConfig 开启 JWT 认证拦截（之前所有路径 permitAll），配置 401/403 统一 JSON 响应；3) 前端路由守卫改为解析 JWT exp 判定过期（之前仅检查 token 字符串存在导致跳过登录页）；4) 登录 store 中 getUserInfo() 失败时回滚清除 token 防止"半登录"状态。
>- HOTFIX (2026-06-11)：修复 Dashboard/Approval/Expense 模块 500 错误 — 根因是 H2 数据库文件（data/oa-system.mv.db）使用旧版 schema-h2.sql 创建，`CREATE TABLE IF NOT EXISTS` 不会更新已存在的表结构，导致 `appr_instance` 等表的列名与实体映射不一致（Column "APPROVERS_SNAPSHOT" not found）。解决：删除 H2 数据文件重新初始化。⚠️ 注意：修改 schema-h2.sql 后需手动删除 H2 DB 文件。
>- HOTFIX (2026-06-11)：增强错误处理 — `UserServiceImpl.getUserRoles()` / `getCurrentUserInfo()` 增加 try-catch 降级保护；前端 Axios 错误处理增加从响应体提取后端错误消息；前端 Dashboard 兼容嵌套/扁平两种响应结构。
>- DEV-30 (2026-06-11)：实现并行审批策略 — 同一级别支持多个审批人，任一人同意推进下一级，任一人驳回则整体驳回；`resolveApprovers()` 方法 ROLE 类型返回所有角色用户（不再只取第一人）；审批操作后自动作废同级别其他待审批记录（`APPROVAL_AUTO_VOIDED = 4`）；前端模板表单新增审批人配置编辑器（动态增删、类型联动）；新增 4 个并行审批测试用例，累计 33 测试全部通过；seed 数据新增"紧急请假（并行审批）"示例模板。
>- DEV-31 (2026-06-11)：实现日程日历视图 — 前端集成 FullCalendar（@fullcalendar/vue3），支持月/周/日三视图切换；日程数据自动映射为日历事件（按类型着色）；点击日期空白区域快速创建日程（预填日期），点击事件打开编辑对话框；日历翻页自动加载对应日期范围数据；保留原有列表视图，双视图通过按钮一键切换。
- DEV-32 (2026-06-11)：前端性能优化 — 布局层添加 `<keep-alive>` 组件缓存，路由切换时保留业务页面状态（表单/滚动/数据）；Vite 构建配置 `manualChunks` 手动分包，将 element-plus / echarts / fullcalendar / element-icons 拆分为独立 chunk，利用浏览器并行加载和缓存策略减少二次加载体积。
- DEV-33 (2026-06-11)：接口文档完善 — 为全部 15 个 Controller 的 70+ 方法参数添加 `@Parameter` 注解（描述/必填/示例），为 8 个 DTO 和 3 个 VO 类的全部字段添加 `@Schema` 注解（描述/示例/隐藏标记），BaseEntity 基类字段同步标注；Knife4j 文档可读性大幅提升。
- DEV-34 (2026-06-11)：报销附件上传集成 — 新建 `frontend/src/api/file.js`（独立 axios 实例 + multipart/form-data 支持）；报销表单新增附件上传区（el-upload + 类型校验 + 10MB 大小限制），上传成功后自动添加到附件列表（JSON 序列化存入 `attachments` 字段）；编辑时解析已有附件并展示；查看详情对话框显示附件链接（可点击下载）。dev 环境 H2 及 prod 环境 MySQL schema 同步更新。
- DEV-35 (2026-06-11)：部门用户统计接口 — 后端新增 `GET /oa/system/dept/user-stats` 接口，返回每个部门的直接人数（`directCount`）和含子部门的总人数（`totalCount`），递归收集子部门ID聚合统计；前端部门管理表格新增「本部门人数」和「总人数（含下级）」两列，使用 el-tag 展示。
- DEV-36 (2026-06-11)：日程提醒通知功能 — `sch_schedule` 表新增 `reminder_minutes`（提前提醒分钟数）和 `reminder_sent`（提醒是否已发送）字段；后端新增 `GET /oa/schedule/reminders` 接口查询未来24小时内未发送提醒的日程；数据看板新增「近期日程提醒」卡片（显示未来24小时日程，含优先级标签、时间倒计时、地点）；前端日程表单新增提醒下拉选择（5分钟~1天），编辑时回填已有提醒设置。
- DEV-37 (2026-06-11)：日程可见性规则细化 — `sch_schedule` 表新增 `visibility` 字段（1=仅自己，2=本部门可见，3=公开可见）；`listByDateRange` 查询增强：公开日程所有人可见、部门日程同部门用户可见、私有日程仅创建人和参与人可见；前端日程表单新增可见性选择（el-select + 图标提示），编辑时回填已有可见性设置。
---

## 目录

1. [项目概述](#1-项目概述)
2. [技术架构](#2-技术架构)
3. [项目结构](#3-项目结构)
4. [功能模块设计](#4-功能模块设计)
5. [数据库设计](#5-数据库设计)
6. [API 接口设计](#6-api-接口设计)
7. [安全设计](#7-安全设计)
8. [开发路线图](#8-开发路线图)
9. [开发规范](#9-开发规范)
10. [部署说明](#10-部署说明)

---

## 1. 项目概述

### 1.1 项目背景

OA（Office Automation，办公自动化）系统是企业信息化建设的核心组件，用于处理企业日常行政事务，包括员工管理、考勤打卡、请假审批、公告通知、日程管理、费用报销等。

### 1.2 核心目标

- **角色明确**：管理员、部门经理、普通员工三种角色，各司其职
- **流程闭环**：请假/报销需走完整审批链，杜绝越权操作
- **数据可视化**：考勤统计、审批效率等关键指标图表展示
- **可扩展**：模块化设计，新增业务模块不影响已有功能

### 1.3 用户角色

| 角色 | 权限范围 | 核心功能 |
|------|---------|---------|
| 超级管理员 | 全部数据 + 全部功能 | 用户管理、角色分配、系统配置、全部审批终审 |
| 部门经理 | 本部门及下级部门 | 下属考勤查看、请假/报销审批、公告发布 |
| 普通员工 | 仅个人数据 | 打卡、请假申请、报销申请、日程管理 |

---

## 2. 技术架构

### 2.1 整体架构

```
┌─────────────────────────────────────────────────────┐
│                    前端（Vue 3 + Element Plus）        │
│                   http://localhost:5173               │
└──────────────────────┬──────────────────────────────┘
                       │ RESTful API (JSON + JWT)
┌──────────────────────▼──────────────────────────────┐
│                  Spring Boot 3.2 (JDK 21)             │
│  ┌──────────┐ ┌──────────┐ ┌──────────────────────┐ │
│  │Controller│→│ Service  │→│ Mapper (MyBatis-Plus) │ │
│  └──────────┘ └──────────┘ └──────────┬───────────┘ │
│  ┌──────────┐                ┌────────▼───────────┐ │
│  │ Filter   │                │       MySQL/H2       │ │
│  │ JWT Auth │                └────────────────────┘ │
│  └──────────┘                                        │
└─────────────────────────────────────────────────────┘
```

### 2.2 技术选型

| 层次 | 技术 | 版本 | 说明 |
|------|------|------|------|
| JDK | OpenJDK | 21 LTS | 虚拟线程支持 |
| 框架 | Spring Boot | 3.2.7 | 开箱即用，生态成熟 |
| ORM | MyBatis-Plus | 3.5.6 | 增强 MyBatis，无 SQL 编程 |
| 安全 | Spring Security + JWT | 6.x + jjwt 0.12 | 无状态认证，RBAC 权限 |
| 数据库 | MySQL (生产) / H2 (开发) | 8.0 / 2.x | H2 支持零配置启动 |
| API 文档 | Knife4j (Swagger 增强) | 4.3 | 接口文档自动生成，在线调试 |
| 工具库 | Hutool | 5.8 | 集合、日期、加密等工具方法 |
| 简化代码 | Lombok | 1.18 | 自动生成 Getter/Setter/构造器 |
| 构建 | Maven | 3.9 | 依赖管理、打包部署 |

### 2.3 关键依赖说明

```xml
<!-- pom.xml 中已配置的核心依赖 -->
spring-boot-starter-web           ← Web MVC + 内嵌 Tomcat
spring-boot-starter-security      ← 认证授权框架
spring-boot-starter-validation   ← Bean Validation
mybatis-plus-spring-boot3-starter ← ORM 框架
mysql-connector-j / h2            ← 数据库驱动
jjwt-api + jjwt-impl + jjwt-jackson ← JWT 令牌
knife4j-openapi3-jakarta-starter ← API 文档
hutool-all                        ← 通用工具
lombok                            ← 代码简化
```

---

## 3. 项目结构

```
oa-system/
├── pom.xml                              # Maven 配置（依赖、插件）
├── README.md                            # 项目说明
├── .gitignore                           # Git 忽略规则
├── Dockerfile                           # Docker 多阶段构建
├── docker-compose.yml                   # Docker Compose（MySQL + OA应用）
├── doc/
│   └── DESIGN.md                        # 本设计文档
├── sql/
│   └── schema-mysql.sql                 # MySQL 完整建表脚本
├── frontend/                            # ═══ Vue 3 前端项目 ═══
│   ├── index.html
│   ├── package.json
│   ├── vite.config.js                   # Vite + 代理配置
│   └── src/
│       ├── main.js                      # 入口（Element Plus + Router + Pinia）
│       ├── App.vue                      # 根组件
│       ├── utils/request.js             # Axios 封装（JWT + 401拦截）
│       ├── api/                         # API 模块（auth/user/dashboard）
│       ├── stores/                      # Pinia 状态（user/app）
│       ├── router/index.js              # 路由表 + 守卫
│       ├── layout/index.vue             # 主布局（侧边栏 + Header）
│       └── views/                       # 页面视图
│           ├── login/                   # 登录页
│           ├── dashboard/               # 数据看板
│           ├── system/                  # 系统管理（用户/角色/菜单/部门）
│           ├── attendance/              # 考勤管理
│           ├── approval/                # 审批中心
│           ├── notice/                  # 公告通知
│           ├── schedule/                # 日程管理
│           └── expense/                 # 报销管理
└── src/
    ├── main/
    │   ├── java/com/oasystem/
    │   │   ├── OaApplication.java           # Spring Boot 入口
    │   │   ├── common/                       # 通用层
    │   │   │   ├── Result.java               # 统一响应 Result<T>
    │   │   │   ├── PageResult.java           # 分页结果 PageResult<T>
    │   │   │   ├── BaseEntity.java           # 实体基类（id, createTime, updateTime, deleted...）
    │   │   │   ├── MyMetaObjectHandler.java  # 自动填充 createBy/updateBy
    │   │   │   ├── constant/
    │   │   │   │   └── Constants.java        # 业务常量（审批状态/考勤状态/业务类型等）
    │   │   │   └── exception/
    │   │   │       ├── BusinessException.java     # 业务异常
    │   │   │       └── GlobalExceptionHandler.java # 全局异常处理
    │   │   ├── config/                       # Spring 配置
    │   │   │   ├── SecurityConfig.java       # Spring Security（JWT + RBAC）
    │   │   │   ├── CorsConfig.java           # 跨域 CORS
    │   │   │   ├── MyBatisPlusConfig.java    # MP 分页插件 + MapperScan
    │   │   │   └── WebMvcConfig.java         # Web MVC（拦截器 + 静态资源映射）
    │   │   ├── security/                     # 安全认证
    │   │   │   ├── JwtTokenProvider.java     # JWT 生成/解析/验证
    │   │   │   ├── JwtAuthenticationFilter.java # 请求过滤器
    │   │   │   ├── JwtUserDetails.java       # 用户详情
    │   │   │   ├── UserDetailsServiceImpl.java # UserDetailsService 实现
    │   │   │   └── SecurityUtils.java        # 获取当前用户工具类
    │   │   │
    │   │   ├── system/                       # ═══ 模块1: 系统管理 ═══
    │   │   │   ├── controller/
    │   │   │   │   ├── AuthController.java       # 登录/登出/用户信息
    │   │   │   │   ├── UserController.java       # 用户 CRUD + 重置密码
    │   │   │   │   ├── RoleController.java       # 角色 CRUD + 菜单分配
    │   │   │   │   ├── DeptController.java       # 部门 CRUD + 部门树
    │   │   │   │   ├── MenuController.java       # 菜单/权限管理
    │   │   │   │   ├── FileController.java       # 文件上传
    │   │   │   │   ├── DashboardController.java  # 数据看板
    │   │   │   │   └── HomeController.java       # 根路径健康检查
    │   │   │   ├── service/
    │   │   │   │   ├── UserService.java
    │   │   │   │   ├── RoleService.java
    │   │   │   │   ├── DeptService.java
    │   │   │   │   └── MenuService.java
    │   │   │   ├── service/impl/
    │   │   │   │   ├── UserServiceImpl.java      # 登录认证 + 用户管理
    │   │   │   │   ├── RoleServiceImpl.java
    │   │   │   │   ├── DeptServiceImpl.java
    │   │   │   │   └── MenuServiceImpl.java
    │   │   │   ├── mapper/
    │   │   │   │   ├── UserMapper.java
    │   │   │   │   ├── RoleMapper.java
    │   │   │   │   ├── DeptMapper.java
    │   │   │   │   ├── MenuMapper.java
    │   │   │   │   ├── UserRoleMapper.java
    │   │   │   │   └── RoleMenuMapper.java
    │   │   │   ├── entity/
    │   │   │   │   ├── User.java / Role.java / Dept.java / Menu.java
    │   │   │   │   ├── UserRole.java / RoleMenu.java
    │   │   │   ├── dto/                      # LoginDTO / UserQueryDTO / RoleDTO / AssignRoleDTO
    │   │   │   └── vo/                       # UserVO / MenuTreeVO / RoleVO
    │   │   │
    │   │   ├── attendance/                   # ═══ 模块2: 考勤管理 ═══
    │   │   │   ├── controller/AttendanceController.java  # 签到/签退/请假/统计
    │   │   │   ├── service/AttendanceService.java
    │   │   │   ├── service/impl/AttendanceServiceImpl.java  # 请假自动创建审批实例
    │   │   │   ├── mapper/AttendanceMapper.java + LeaveRequestMapper.java
    │   │   │   ├── entity/Attendance.java + LeaveRequest.java
    │   │   │   └── dto/SignInDTO.java + LeaveRequestDTO.java
    │   │   │
    │   │   ├── approval/                     # ═══ 模块3: 审批中心 ═══
    │   │   │   ├── controller/
    │   │   │   │   ├── ApprovalController.java        # 发起/待审批/已审批/审批/撤回/记录
    │   │   │   │   └── ApprovalTemplateController.java # 模板 CRUD
    │   │   │   ├── service/
    │   │   │   │   ├── ApprovalService.java           # 含 cancel 撤回接口
    │   │   │   │   └── ApprovalTemplateService.java
    │   │   │   ├── service/impl/
    │   │   │   │   ├── ApprovalServiceImpl.java       # 多级审批引擎 + syncBusinessStatus 回调
    │   │   │   │   └── ApprovalTemplateServiceImpl.java
    │   │   │   ├── mapper/ApprovalTemplateMapper.java + ApprovalInstanceMapper.java + ApprovalRecordMapper.java
    │   │   │   ├── entity/ApprovalTemplate.java + ApprovalInstance.java + ApprovalRecord.java
    │   │   │   └── dto/StartApprovalDTO.java + ApproveDTO.java
    │   │   │
    │   │   ├── notice/                       # ═══ 模块4: 公告通知 ═══
    │   │   │   ├── controller/NoticeController.java
    │   │   │   ├── service/NoticeService.java + impl/NoticeServiceImpl.java
    │   │   │   ├── mapper/NoticeMapper.java
    │   │   │   └── entity/Notice.java
    │   │   │
    │   │   ├── schedule/                     # ═══ 模块5: 日程管理 ═══
    │   │   │   ├── controller/ScheduleController.java
    │   │   │   ├── service/ScheduleService.java + impl/ScheduleServiceImpl.java
    │   │   │   ├── mapper/ScheduleMapper.java
    │   │   │   └── entity/Schedule.java
    │   │   │
    │   │   ├── expense/                      # ═══ 模块6: 报销管理 ═══
    │   │   │   ├── controller/ExpenseController.java
    │   │   │   ├── service/ExpenseService.java
    │   │   │   ├── service/impl/ExpenseServiceImpl.java  # 报销自动创建审批实例
    │   │   │   ├── mapper/ExpenseMapper.java
    │   │   │   └── entity/ExpenseRequest.java
    │   │   │
    │   │   └── log/                          # ═══ 操作日志（AOP） ═══
    │   │       ├── annotation/Log.java       # @Log 操作日志注解
    │   │       ├── aspect/OperLogAspect.java # AOP 切面
    │   │       ├── controller/OperLogController.java  # 日志查询/清理
    │   │       ├── service/OperLogService.java + impl/OperLogServiceImpl.java
    │   │       ├── mapper/OperLogMapper.java
    │   │       └── entity/OperLog.java
    │   │
    │   └── resources/
    │       ├── application.yml               # 主配置（端口、MP、JWT、Knife4j）
    │       ├── application-dev.yml           # 开发环境（H2 数据库）
    │       ├── application-prod.yml          # 生产环境（MySQL + HikariCP + 日志）
    │       ├── db/
    │       │   ├── schema-h2.sql             # H2 建表脚本
    │       │   └── data.sql                  # 初始数据（admin + 菜单 + 审批模板）
    │       └── mapper/
    │           └── UserMapper.xml            # MyBatis XML（用户复杂查询）
    │           ├── RoleMapper.xml            # 角色-用户联查
    │           ├── MenuMapper.xml            # 菜单-角色-用户联查
    │           └── AttendanceMapper.xml      # 考勤按月查询
    │
    └── test/
        └── java/com/oasystem/
            ├── OaApplicationTests.java       # Spring Boot 集成测试
            ├── approval/service/impl/ApprovalServiceImplTest.java  # 审批引擎 15 用例
            ├── attendance/service/impl/AttendanceServiceImplTest.java  # 考勤集成 6 用例
            └── system/service/impl/UserServiceImplTest.java  # 用户管理 8 用例（共 22 测试）

---

## 4. 功能模块设计

### 4.1 模块1：系统管理（✅ 基础已实现）

这是整个系统的基石，负责用户认证和权限管理。

#### 4.1.1 用户管理（已实现）

- 用户登录：✅ POST `/oa/auth/login`，返回 JWT Token
- 获取当前用户信息：✅ GET `/oa/auth/user-info`
- 分页查询用户：✅ GET `/oa/system/user/page`
- 新增用户：✅ POST `/oa/system/user`（密码 BCrypt 加密）
- 更新用户：✅ PUT `/oa/system/user`
- 删除用户：✅ DELETE `/oa/system/user/{id}`（逻辑删除）
- 重置密码：✅ PUT `/oa/system/user/{id}/reset-pwd`（重置为 123456）

#### 4.1.2 角色管理（✅ 已实现）

- 角色 CRUD：✅ 已实现，包含编码唯一性校验、删除前检查用户引用
- 用户-角色关联：✅ 接口与服务（覆盖式分配/移除）已实现
- 角色-菜单分配：✅ 已实现菜单权限批量分配

#### 4.1.3 部门管理（✅ 已实现 + 用户统计）

- 部门树查询：✅ 基本实现（扁平列表 + 简单构造树）
- 部门 CRUD：✅ 新增/更新/删除接口已实现，写操作受管理员权限限制
- 部门用户统计 (DEV-35)：✅ 各部门直接人数 + 含子部门总人数统计接口 `GET /system/dept/user-stats`

#### 4.1.4 菜单管理（已实现基础功能 + 安全增强）

- 菜单树查询：✅ `/system/menu/tree` 已实现（支持按用户权限或全部列出）
- 菜单 CRUD：✅ 新增/更新/删除已实现（包含 `perms` 唯一性校验）
- 菜单删除安全性：✅ 删除时清理 role-menu 关联；已存在的安全增强（禁止删除被角色引用的菜单，需先移除引用）
- 前端动态路由集成：后端已提供树形数据，前端集成为后续任务

---

### 4.2 模块2：考勤管理（✅ 已实现基础功能 + 统计增强）

当前实现（DEV-08 + DEV-18 + DEV-22）：
- 每日打卡：✅ 签到/签退自动判定迟到（晚于9:00）/早退（早于18:00），自动计算工作时长
- 考勤记录查询：✅ 按月份查询（`/attendance/records`）
- 请假申请：✅ 提交请假自动创建审批实例走多级审批流，审批完成后自动同步请假状态
- 月度汇总统计：✅ 出勤/迟到/早退/缺勤天数 + 总工时 + 请假天数（`/attendance/monthly-report`）
- 每日状态明细：✅ 全月每日考勤状态，周末自动识别（`/attendance/daily-status`）
- 考勤图表可视化：✅ 前端数据看板已集成 ECharts 考勤趋势折线图（DEV-29）

---

### 4.3 模块3：审批中心（✅ 多级审批引擎已实现）

当前实现（DEV-10 + DEV-16 + DEV-22）：
- 审批模板管理：✅ 模板 CRUD 已实现（`ApprovalTemplateController`），模板中配置审批人规则（DEPT_LEADER/ROLE/USER）
- 发起审批：✅ 自动解析审批人、生成快照、预创建各级审批记录
- 待审批列表：✅ 按当前用户+当前审批级别精确匹配
- 已审批/我的申请：✅ 按用户过滤并排序
- 审批操作：✅ 同意推进下一级/驳回结束，严格校验审批人身份
- 审批记录查询：✅ 按实例ID查询各级审批意见
- 业务回调：✅ 审批完成后自动同步关联业务记录状态（请假 ✅，报销 ✅）
- 审批撤回：✅ 申请人可撤回审批中的实例，同步取消关联业务记录
- **并行审批 (DEV-30)**：✅ 模板中允许同一 `level` 出现多个审批人配置项。每个配置独立解析，生成多条快照共享同一级别。同一级别的任一审批人同意即推进到下一级，任一审批人驳回则整个实例驳回。处理后同级别其他待审批记录自动标记为"作废"（`APPROVAL_AUTO_VOIDED=4`）。ROLE 类型返回所有拥有该角色的用户（不再只取第一个）。前端模板表单支持动态添加/删除审批人配置项。

---

### 4.4 模块4：公告通知（✅ 基础已实现）

- 公告列表/详情：基础查询与分页已实现
- 发布/编辑/删除：已实现，写操作受管理员权限限制
- 阅读统计、定时发布、富文本附件等为后续增强项

---

### 4.5 模块5：日程管理（✅ 完整实现）

当前实现（DEV-15 + DEV-31 + DEV-36 + DEV-37）：
- 日程 CRUD：✅ 新增/更新/删除/详情接口已实现（`ScheduleController`）
- 按日期范围查询：✅ 支持按开始日期、结束日期过滤日程列表
- 个人权限控制：✅ 仅创建人可修改/删除自己的日程
- 参与人支持：✅ 日程可指定参与人列表（JSON 数组存储）
- **日历视图 (DEV-31)**：✅ 前端集成 FullCalendar，支持月/周/日三视图切换，日程按类型着色，点击日期快速创建，点击事件编辑，日历翻页自动加载数据
- **日程提醒 (DEV-36)**：✅ 支持设置提前提醒（5分钟~1天），数据看板展示未来24小时提醒，倒计时显示
- **可见性规则 (DEV-37)**：✅ 私有（仅自己）/ 部门可见 / 公开 三级可见性，查询时自动按可见性+部门过滤

---

### 4.6 模块6：报销管理（✅ 完整实现）

当前实现（DEV-17 + DEV-25 + DEV-34）：
- 报销申请 CRUD：✅ 提交/列表查询/详情/修改/删除接口已实现（`ExpenseController`）
- 个人权限控制：✅ 只能查看/修改/删除自己的报销申请，且仅待审批状态可修改/删除
- 按状态过滤：✅ 列表支持按审批状态（审批中/已通过/已驳回）过滤
- 报销统计：✅ 按状态汇总申请数量和金额（`/expense/stats`）
- 金额校验：✅ 报销金额必须大于零
- 审批集成：✅ 提交报销自动创建审批实例走多级审批流，审批完成后自动同步报销状态
- **附件上传 (DEV-34)**：✅ 支持上传图片/PDF/Office 等文件（10MB限制），上传后自动关联到报销申请，附件列表可增删，查看详情可下载

---

## 5. 数据库设计

### 5.1 ER 关系

```
┌──────────┐     ┌──────────────┐     ┌──────────┐
│  sys_dept │←──│   sys_user    │──→│sys_user_role│←──│ sys_role │
└──────────┘   │               │   └──────────────┘   └──────────┘
               └───────────────┘                         │
                                                        ↓
                                                 ┌──────────────┐
                                                 │sys_role_menu │
                                                 └──────┬───────┘
                                                        ↓
                                                   ┌──────────┐
                                                   │ sys_menu │
                                                   └──────────┘
```

### 5.2 核心表一览

#### 系统管理（5 表）

| 表名 | 说明 | 关键字段 |
|------|------|---------|
| `sys_user` | 用户表 | username, password(BCrypt), dept_id, status |
| `sys_role` | 角色表 | role_name, role_code, data_scope |
| `sys_dept` | 部门表 | parent_id, dept_name, ancestors |
| `sys_menu` | 菜单/权限表 | parent_id, menu_type, perms, path, component |
| `sys_user_role` | 用户-角色关联 | user_id, role_id (联合主键) |
| `sys_role_menu` | 角色-菜单关联 | role_id, menu_id (联合主键) |

#### 考勤模块（2 表）

| 表名 | 说明 | 关键字段 |
|------|------|---------|
| `att_record` | 考勤记录 | user_id, attendance_date, sign_in_time, sign_out_time, status, work_hours |
| `att_leave_request` | 请假申请 | user_id, leave_type, start_date, end_date, days, status, approval_instance_id |

#### 审批模块（3 表）

| 表名 | 说明 | 关键字段 |
|------|------|---------|
| `appr_template` | 审批模板 | template_name, template_code, approvers_config(JSON), approval_levels |
| `appr_instance` | 审批实例 | template_id, applicant_id, current_level, status, approvers_snapshot(JSON), business_type, business_id |
| `appr_record` | 审批记录 | instance_id, level, approver_id, result |

#### 其他模块（4 表）

| 表名 | 说明 | 关键字段 |
|------|------|---------|
| `sys_notice` | 公告通知 | title, content, notice_type, status, is_top |
| `sch_schedule` | 日程管理 | title, start_time, end_time, creator_id, participant_ids |
| `exp_request` | 报销申请 | user_id, title, amount, expense_type, status, approval_instance_id |
| `sys_oper_log` | 操作日志（AOP自动记录） | user_id, module, operation, url, status, cost_time |

### 5.3 通用字段说明

所有业务表继承 `BaseEntity`，包含以下通用字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT | 自增主键 |
| `create_time` | DATETIME | 创建时间（自动填充） |
| `update_time` | DATETIME | 更新时间（自动填充） |
| `create_by` | BIGINT | 创建人ID（自动填充） |
| `update_by` | BIGINT | 更新人ID（自动填充） |
| `deleted` | TINYINT | 逻辑删除（0=正常，1=已删除），MyBatis-Plus 自动处理 |
| `remark` | VARCHAR(500) | 备注 |

---

## 6. API 接口设计

### 6.1 统一响应格式

所有 API 返回 JSON，结构如下：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... },
  "timestamp": 1718035200000
}
```

### 6.2 HTTP 状态码约定

| 状态码 | 含义 |
|--------|------|
| 200 | 请求成功 |
| 400 | 参数校验失败 |
| 401 | 未登录或 Token 过期 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### 6.3 已实现接口

#### 认证模块 `POST/GET /oa/auth`

```
POST   /oa/auth/login          ← 登录，返回 Token
GET    /oa/auth/user-info      ← 获取当前用户信息
POST   /oa/auth/logout         ← 登出
```

#### 用户管理 `GET/POST/PUT/DELETE /oa/system/user`

```
GET    /oa/system/user/page     ← 分页查询用户
GET    /oa/system/user/{id}     ← 根据ID查询
POST   /oa/system/user          ← 新增用户
PUT    /oa/system/user          ← 更新用户
DELETE /oa/system/user/{id}     ← 删除用户（逻辑删除）
PUT    /oa/system/user/{id}/reset-pwd  ← 重置密码
```

### 6.4 业务模块接口（✅ 全部已实现）

#### 角色管理 `/oa/system/role`（✅ 已实现）

```
GET    /oa/system/role/list     ← 角色列表
GET    /oa/system/role/{id}     ← 角色详情
POST   /oa/system/role          ← 新增角色
PUT    /oa/system/role          ← 更新角色
DELETE /oa/system/role/{id}     ← 删除角色
PUT    /oa/system/role/{id}/menus ← 分配菜单权限
```

#### 部门管理 `/oa/system/dept`（✅ 已实现 + 用户统计）

```
GET    /oa/system/dept/tree       ← 部门列表（树形结构）
GET    /oa/system/dept/user-stats ← 各部门用户统计（含子部门汇总）
GET    /oa/system/dept/{id}      ← 部门详情
POST   /oa/system/dept           ← 新增部门
PUT    /oa/system/dept           ← 更新部门
DELETE /oa/system/dept/{id}      ← 删除部门
```

#### 菜单管理 `/oa/system/menu`（✅ 已实现）

```
GET    /oa/system/menu/tree      ← 菜单树（按用户权限）
POST   /oa/system/menu           ← 新增菜单
PUT    /oa/system/menu           ← 更新菜单
DELETE /oa/system/menu/{id}      ← 删除菜单
```

#### 考勤管理 `/oa/attendance`（✅ 已实现 + 统计增强）

```
POST   /oa/attendance/sign-in        ← 签到（自动判定迟到）
POST   /oa/attendance/sign-out       ← 签退（自动判定早退+计算工时）
GET    /oa/attendance/records?month= ← 考勤记录（按月份）
POST   /oa/attendance/leave          ← 请假申请
GET    /oa/attendance/monthly-report?month= ← 月度汇总统计
GET    /oa/attendance/daily-status?month=   ← 每日考勤状态明细
```

#### 审批中心 `/oa/approval`（✅ 多级审批引擎已实现）

```
POST   /oa/approval/start       ← 发起审批（自动解析审批人+预创建记录）
GET    /oa/approval/todo        ← 待审批列表（按当前用户+当前级别匹配）
GET    /oa/approval/done        ← 已审批列表
GET    /oa/approval/my          ← 我的申请
POST   /oa/approval/{id}/approve ← 审批操作（同意/驳回，身份验证）
POST   /oa/approval/{id}/cancel  ← 撤回审批（仅申请人可撤回审批中的实例）
GET    /oa/approval/{id}/records ← 审批记录

GET    /oa/approval/template/list  ← 模板列表
GET    /oa/approval/template/{id}  ← 模板详情
POST   /oa/approval/template       ← 新增模板（Admin）
PUT    /oa/approval/template       ← 更新模板（Admin）
DELETE /oa/approval/template/{id}  ← 删除模板（Admin）
```

#### 公告通知 `/oa/notice`

```
GET    /oa/notice/page          ← 公告列表（分页）
GET    /oa/notice/{id}          ← 公告详情
POST   /oa/notice               ← 发布公告
PUT    /oa/notice               ← 编辑公告
DELETE /oa/notice/{id}          ← 删除公告
```

#### 日程管理 `/oa/schedule`（✅ 已实现 + 提醒 + 可见性）

```
GET    /oa/schedule/list?startDate=&endDate=  ← 日程列表（按日期范围+可见性过滤）
GET    /oa/schedule/reminders   ← 即将到来的日程提醒（未来24小时）
GET    /oa/schedule/{id}        ← 日程详情
POST   /oa/schedule             ← 新增日程
PUT    /oa/schedule             ← 更新日程
DELETE /oa/schedule/{id}        ← 删除日程
```

#### 报销管理 `/oa/expense`（✅ 已实现）

```
GET    /oa/expense/list?status= ← 我的报销列表（可选按状态过滤）
GET    /oa/expense/{id}         ← 报销详情
POST   /oa/expense              ← 提交报销申请
PUT    /oa/expense              ← 修改报销申请（仅待审批状态）
DELETE /oa/expense/{id}         ← 删除报销申请（仅待审批状态）
GET    /oa/expense/stats        ← 报销统计（按状态汇总数量和金额）
```

#### 操作日志 `/oa/log`（✅ 已实现，Admin）

```
GET    /oa/log/page?pageNum=&pageSize=&username=&module=&status= ← 日志列表（分页）
GET    /oa/log/{id}              ← 日志详情
DELETE /oa/log/clean?days=90     ← 清理旧日志
```

#### 文件上传 `/oa/file`（✅ 已实现）

```
POST   /oa/file/upload           ← 通用文件上传（返回URL）
POST   /oa/file/upload/avatar    ← 头像上传（仅限图片格式）
```

#### 数据看板 `/oa/dashboard`（✅ 已实现）

```
GET    /oa/dashboard/overview             ← 总览（系统/考勤/审批/报销概要）
GET    /oa/dashboard/attendance-trend     ← 近7天考勤趋势
GET    /oa/dashboard/approval-distribution ← 审批状态分布（饼图）
GET    /oa/dashboard/expense-distribution  ← 报销类型分布（饼图）
```

### 6.5 Knife4j 文档

开发阶段访问：`http://localhost:8080/oa/doc.html`

---

## 7. 安全设计

### 7.1 认证流程

```
1. 用户提交用户名+密码 → POST /oa/auth/login
2. 服务端验证 → 生成 JWT Token → 返回 Token
3. 前端存储 Token（localStorage）→ 后续请求携带在 Header
4. JwtAuthenticationFilter 拦截请求 → 解析 Token → 设置 SecurityContext
```

### 7.2 JWT Token 结构

```json
{
  "sub": "1",            // 用户ID
  "username": "admin",   // 用户名
  "iat": 1718035200,     // 签发时间
  "exp": 1718121600      // 过期时间（24小时）
}
```

### 7.3 RBAC 权限模型

```
用户(User) ──N:M──→ 角色(Role) ──N:M──→ 菜单/权限(Menu)
                                          │
                          ┌───────┬────────┴────────┬───────┐
                         目录     菜单              按钮
                       (分组)   (路由/页面)    (操作权限如 add/edit/delete)
```

### 7.4 密码安全

- 使用 BCrypt 加密存储，不可逆
- 默认密码：`123456`（首次登录后建议修改）
- 密码长度 ≥ 6 位

### 7.5 当前安全状态

- ✅ **JWT 认证已全面启用**（2026-06-11 修复）：
  - `SecurityConfig` 配置：除 `/auth/**`（登录）、Swagger/H2 控制台外，所有请求需要认证；
  - 未认证请求返回 401 JSON（`authenticationEntryPoint`），权限不足返回 403 JSON（`accessDeniedHandler`）；
  - `JwtAuthenticationFilter` 解析 Token → `UserDetailsServiceImpl` 加载角色+菜单权限 → 填充 `SecurityContext`；
  - 接口级权限通过 `@PreAuthorize` 注解控制（如 `ROLE_ADMIN` 才可管理模板/日志/部门/角色等）；
  - `SecurityUtils` 提供 `getCurrentUserId()` / `getCurrentUsername()` / `hasPermission()` 便捷方法；
  - 前端路由守卫解析 JWT `exp` 判定过期，Axios 拦截器统一处理 401 跳转登录页。

---

## 8. 开发路线图

### Phase 1：基础框架（✅ 已完成）

- [x] 项目骨架搭建（Maven + Spring Boot）
- [x] 数据库表设计 + 初始化脚本
- [x] 通用组件（Result、PageResult、BaseEntity、异常处理）
- [x] Spring Security + JWT 基础配置
- [x] 用户登录 + 用户管理 CRUD
- [x] H2 内置数据库（开箱即用运行）

### Phase 2：系统管理完善（✅ 已完成）

- [x] 完成 Spring Security + JWT 认证集成（DEV-05）
- [x] 角色管理 CRUD + 菜单树（DEV-04 / DEV-11）
- [x] 部门管理 CRUD + 部门树（DEV-07）
- [x] 菜单管理 + 动态路由数据源（DEV-06 / DEV-13 / DEV-14）
- [x] 用户-角色分配、角色-菜单分配（DEV-04）
- [x] 前端项目初始化（Vue 3 + Element Plus）+ 业务页面完善（DEV-28 / DEV-29）

### Phase 3：核心业务模块（优先级：中 🟡）

- [x] 考勤打卡（签到/签退 — DEV-08）
- [x] 请假申请 + 审批流程（DEV-08）
- [x] 审批中心（模板 + 实例 + 多级流转 — DEV-10 + DEV-16）
- [x] 公告通知（发布/列表/置顶 — DEV-09）

### Phase 4：扩展模块（✅ 已完成）

- [x] 日程管理（DEV-15）
- [x] 费用报销（DEV-17）
- [x] 操作日志记录（DEV-19）
- [x] 文件上传（头像/附件 — DEV-20）
- [x] 数据看板 API（DEV-21）

### Phase 5：上线准备

- [x] 切换到 MySQL（DEV-23：application-prod.yml + HikariCP 连接池 + 日志）
- [x] 单元测试（DEV-24：JUnit 5 + Mockito 覆盖 Approval/Attendance/User 三大核心 Service）
- [x] 性能优化（路由懒加载 + keep-alive 缓存 + 构建分包 — DEV-32）
- [x] Docker 部署脚本（DEV-27：Dockerfile 多阶段构建 + docker-compose.yml）
- [x] 接口文档完善（DEV-33：@Parameter + @Schema 全覆盖）

---

## 9. 开发规范

### 9.1 代码规范

1. **分层调用**：Controller → Service（接口） → ServiceImpl → Mapper  
   禁止 Controller 直接调用 Mapper，禁止 Service 跨模块调用 Mapper

2. **DTO/VO 分离**：  
   - DTO（Data Transfer Object）：接收前端传来的请求参数  
   - VO（View Object）：返回给前端的脱敏数据  
   - Entity：与数据库表一一对应

3. **异常处理**：业务异常统一抛 `BusinessException`，由 `GlobalExceptionHandler` 捕获

4. **返回值**：Controller 的返回值统一用 `Result<T>` 或 `PageResult<T>` 包装

5. **日志规范**：使用 Lombok 的 `@Slf4j`，关键操作记录 info 日志

6. **注释规范**：每个类写清楚 JavaDoc 说明其职责

### 9.2 数据库规范

1. 表名使用小写 + 下划线，模块前缀区分：
   - `sys_` — 系统管理
   - `att_` — 考勤
   - `appr_` — 审批
   - `sch_` — 日程
   - `exp_` — 报销

2. 字段名使用小写 + 下划线，Java 实体使用驼峰（MyBatis-Plus 自动转换）

3. 必须填写 COMMENT 注释

4. 外键不使用物理外键，在业务层保证完整性

### 9.3 Git 提交规范

```
feat: 新功能（如 feat: 完成用户管理CRUD）
fix: 修复bug（如 fix: 修复登录Token过期不刷新）
refactor: 重构（如 refactor: 优化审批流转逻辑）
docs: 文档（如 docs: 更新API文档）
style: 格式调整
test: 测试
```

---

## 10. 部署说明

### 10.1 开发环境运行（H2 内嵌数据库，推荐开发时使用）

```bash
# 1. 进入项目目录
cd D:\CLion\oa-system

# 2. 启动应用（H2 自动初始化）
D:\CLion\tools\apache-maven-3.9.16\bin\mvn spring-boot:run

# 3. 访问
# 应用: http://localhost:8080/oa
# API文档: http://localhost:8080/oa/doc.html
# H2控制台: http://localhost:8080/oa/h2-console
#   - JDBC URL: jdbc:h2:file:./data/oa-system
#   - 用户名: sa，密码: 空
```

### 10.2 切换到 MySQL（生产环境）

**方式一：使用 prod profile（推荐）**

1. 安装 MySQL 8.0
2. 创建数据库：`CREATE DATABASE oa_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`
3. 导入建表脚本：`mysql -u root -p oa_system < sql/schema-mysql.sql`
4. 修改 `application-prod.yml` 中的数据库连接信息（用户名/密码）
5. 启动时指定 prod profile：
   ```bash
   java -jar target/oa-system.jar --spring.profiles.active=prod
   ```
   或在 `application.yml` 中修改 `spring.profiles.active` 为 `prod`

**方式二：直接修改 dev profile**

修改 `application-dev.yml` 中的数据源配置，注释 H2 部分，启用 MySQL 部分。

### 10.3 Docker 部署（推荐生产环境）

```bash
# 1. 进入项目目录
cd D:\CLion\oa-system

# 2. 启动所有服务（MySQL + OA 应用）
docker-compose up -d

# 3. 查看日志
docker-compose logs -f oa-app

# 4. 停止服务
docker-compose down

# 5. 停止并清除数据卷
docker-compose down -v
```

访问：`http://localhost:8080/oa`

**Docker 架构说明**：
- `mysql` 容器：MySQL 8.0，首次启动自动执行 `sql/schema-mysql.sql` 建表
- `oa-app` 容器：Spring Boot 应用，等待 MySQL 健康检查通过后启动
- 数据持久化：MySQL 数据、上传文件、应用日志均通过 Docker Volume 持久化

### 10.4 打包部署

```bash
# 打包为可执行 JAR
mvn clean package -DskipTests

# 运行
java -jar target/oa-system.jar --spring.profiles.active=prod
```

### 10.5 CLion 运行配置

1. 打开 `D:\CLion\oa-system` 目录
2. CLion → File → Settings → Build → Maven → 设置 Maven home 为 `D:\CLion\tools\apache-maven-3.9.16`
3. 右键 `OaApplication.java` → Run 'OaApplication'
4. 或使用 Maven 面板 → Plugins → spring-boot → spring-boot:run

---

## 附录

### A. 关键技术文档链接

- Spring Boot 3.x: https://docs.spring.io/spring-boot/docs/current/reference/html/
- MyBatis-Plus: https://baomidou.com/
- Spring Security: https://docs.spring.io/spring-security/reference/
- Knife4j: https://doc.xiaominfo.com/
- Hutool: https://www.hutool.cn/

### B. 常见问题

**Q: 启动报 "Table not found"？**
A: 检查 `application-dev.yml` 中 `spring.sql.init.mode` 是否为 `always`，确保启动时自动执行建表脚本。

**Q: 修改了 schema-h2.sql 但表结构没变化 / 出现 "Column not found" 错误？**
A: `CREATE TABLE IF NOT EXISTS` 不会更新已存在的表。需要删除 H2 数据库文件后重启：
```powershell
Remove-Item D:\CLion\oa-system\data\oa-system.mv.db
```
重启 Spring Boot 后会自动重建数据库。

**Q: 如何查看 H2 数据库内容？**
A: 启动后访问 `http://localhost:8080/oa/h2-console`，JDBC URL 填 `jdbc:h2:file:./data/oa-system`

**Q: 如何添加新模块？**
A: 在 `com.oasystem` 下新建包（如 `warehouse`）→ 添加 entity/mapper/service/controller 子包 → 在 `@MapperScan` 覆盖范围内即可自动扫描。

**Q: MyBatis XML 放哪里？**
A: 放在 `src/main/resources/mapper/` 下，MyBatis-Plus 会自动扫描。

---

> **本文档面向 AI 辅助开发场景编写。后续开发中，任何智能体读取此文档后应能理解项目全貌、当前进度和待办事项，从而精确辅助编码。**
