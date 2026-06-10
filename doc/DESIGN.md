# OA 办公自动化系统 — 项目设计文档

> **版本**: 1.0.0  
> **日期**: 2026-06-10  
> **技术栈**: Spring Boot 3.2 + MyBatis-Plus + MySQL/H2 + Spring Security + JWT  
> **目标**: 完整的企业级 OA 系统，含 6 大模块，可作为课程设计/毕业设计交付

> **迭代记录**：
>
>- DEV-01 (2026-06-10)：基础框架与 JWT 认证集成、用户管理 CRUD、H2 开发环境、API 文档（Knife4j）
>- DEV-02 (2026-06-10)：添加 `.gitignore` 忽略本地构建与 IDE 文件，更新项目文档（README、DESIGN）
- DEV-03 (2026-06-10)：添加根路径健康接口 `GET /`（HomeController），并在 `SecurityConfig` 中允许根路径无鉴权，方便访问应用根地址查看运行状态。
- DEV-04 (2026-06-10)：实现角色管理基础 CRUD、菜单树接口、用户-角色分配（Role/RoleMenu/UserRole），并更新文档与 DB 脚本。
- DEV-04 (2026-06-10)：实现角色管理基础 CRUD、菜单树接口、用户-角色分配（Role/RoleMenu/UserRole），并更新文档与 DB 脚本。
- DEV-05 (2026-06-10)：实现权限注入（UserDetailsService），在 JWT 认证中加载用户权限并填充 SecurityContext，完善 `SecurityUtils` 获取当前用户与权限。
- DEV-05 (2026-06-10)：实现权限注入（UserDetailsService），在 JWT 认证中加载用户权限并填充 SecurityContext，完善 `SecurityUtils` 获取当前用户与权限。
- DEV-06 (2026-06-10)：实现菜单管理 CRUD（新增/删除/查询树）并在敏感接口添加方法级权限注解（@PreAuthorize），完善角色/用户管理的权限校验。
- DEV-07 (2026-06-10)：实现部门管理 CRUD（新增/更新/删除/查询树），并为部门写操作添加管理员权限校验。
- DEV-07 (2026-06-10)：实现部门管理 CRUD（新增/更新/删除/查询树），并为部门写操作添加管理员权限校验。
- DEV-08 (2026-06-10)：实现考勤模块基础（签到/签退/按月查询/请假申请），包含实体、Mapper、Service 与 Controller。文档与 README 已同步更新。


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
├── pom.xml                          # Maven 配置（依赖、插件）
├── README.md                        # 项目说明
├── doc/
│   └── DESIGN.md                    # 本设计文档
├── sql/
│   └── schema-mysql.sql             # MySQL 完整建表脚本
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
    │   │   │   │   └── Constants.java        # 业务常量（审批状态、考勤状态等）
    │   │   │   └── exception/
    │   │   │       ├── BusinessException.java     # 业务异常
    │   │   │       └── GlobalExceptionHandler.java # 全局异常处理
    │   │   ├── config/                       # Spring 配置
    │   │   │   ├── SecurityConfig.java       # Spring Security（JWT + RBAC）
    │   │   │   ├── CorsConfig.java           # 跨域 CORS
    │   │   │   ├── MyBatisPlusConfig.java    # MP 分页插件 + MapperScan
    │   │   │   └── WebMvcConfig.java         # Web MVC（拦截器等）
    │   │   ├── security/                     # 安全认证
    │   │   │   ├── JwtTokenProvider.java     # JWT 生成/解析/验证
    │   │   │   ├── JwtAuthenticationFilter.java # 请求过滤器
    │   │   │   └── SecurityUtils.java        # 获取当前用户工具类
    │   │   │
    │   │   ├── system/                       # ═══ 模块1: 系统管理 ═══
    │   │   │   ├── controller/
    │   │   │   │   ├── AuthController.java   # 登录/登出/用户信息
    │   │   │   │   ├── UserController.java   # 用户 CRUD + 重置密码
    │   │   │   │   ├── RoleController.java   # 角色管理（待实现）
    │   │   │   │   ├── DeptController.java   # 部门管理（待实现）
    │   │   │   │   └── MenuController.java   # 菜单/权限管理（待实现）
    │   │   │   ├── service/                  # 业务接口
    │   │   │   │   └── UserService.java
    │   │   │   ├── service/impl/             # 业务实现
    │   │   │   │   └── UserServiceImpl.java  # 登录认证 + 用户管理
    │   │   │   ├── mapper/                   # 数据访问
    │   │   │   │   ├── UserMapper.java
    │   │   │   │   ├── RoleMapper.java
    │   │   │   │   ├── DeptMapper.java
    │   │   │   │   └── MenuMapper.java
    │   │   │   ├── entity/                   # 数据实体
    │   │   │   │   ├── User.java
    │   │   │   │   ├── Role.java
    │   │   │   │   ├── Dept.java
    │   │   │   │   ├── Menu.java
    │   │   │   │   ├── UserRole.java
    │   │   │   │   └── RoleMenu.java
    │   │   │   ├── dto/                      # 请求参数对象
    │   │   │   │   ├── LoginDTO.java
    │   │   │   │   └── UserQueryDTO.java
    │   │   │   └── vo/                       # 返回视图对象
    │   │   │       ├── UserVO.java
    │   │   │       └── MenuTreeVO.java
    │   │   │
    │   │   ├── attendance/                   # ═══ 模块2: 考勤管理 ═══
    │   │   │   ├── controller/               # 打卡、请假（待实现）
    │   │   │   ├── service/
    │   │   │   ├── service/impl/
    │   │   │   ├── mapper/
    │   │   │   ├── entity/
    │   │   │   │   ├── Attendance.java       # 考勤记录
    │   │   │   │   └── LeaveRequest.java     # 请假申请
    │   │   │   └── dto/
    │   │   │
    │   │   ├── approval/                     # ═══ 模块3: 审批中心 ═══
    │   │   │   ├── controller/               # 发起/审批/查询（待实现）
    │   │   │   ├── service/
    │   │   │   ├── service/impl/
    │   │   │   ├── mapper/
    │   │   │   ├── entity/
    │   │   │   │   ├── ApprovalTemplate.java # 审批模板
    │   │   │   │   ├── ApprovalInstance.java # 审批实例
    │   │   │   │   └── ApprovalRecord.java   # 审批记录
    │   │   │   └── dto/
    │   │   │
    │   │   ├── notice/                       # ═══ 模块4: 公告通知 ═══
    │   │   │   ├── controller/               # 公告发布/列表（待实现）
    │   │   │   ├── service/
    │   │   │   ├── service/impl/
    │   │   │   ├── mapper/
    │   │   │   ├── entity/
    │   │   │   │   └── Notice.java
    │   │   │   └── dto/
    │   │   │
    │   │   ├── schedule/                     # ═══ 模块5: 日程管理 ═══
    │   │   │   ├── controller/               # 日程 CRUD（待实现）
    │   │   │   ├── service/
    │   │   │   ├── service/impl/
    │   │   │   ├── mapper/
    │   │   │   ├── entity/
    │   │   │   │   └── Schedule.java
    │   │   │   └── dto/
    │   │   │
    │   │   └── expense/                      # ═══ 模块6: 报销管理 ═══
    │   │       ├── controller/               # 报销申请/审批（待实现）
    │   │       ├── service/
    │   │       ├── service/impl/
    │   │       ├── mapper/
    │   │       ├── entity/
    │   │       │   └── ExpenseRequest.java
    │   │       └── dto/
    │   │
    │   └── resources/
    │       ├── application.yml               # 主配置（端口、MP、JWT、Knife4j）
    │       ├── application-dev.yml           # 开发环境（H2 数据库）
    │       ├── db/
    │       │   ├── schema-h2.sql             # H2 建表脚本
    │       │   └── data.sql                  # 初始数据（admin + 菜单）
    │       └── mapper/
    │           └── UserMapper.xml            # MyBatis XML（复杂查询）
    │
    └── test/
        └── java/com/oasystem/
            └── OaApplicationTests.java       # Spring Boot 集成测试
```

---

## 4. 功能模块设计

### 4.1 模块1：系统管理（✅ 基础已实现）

这是整个系统的基石，负责用户认证和权限管理。

#### 4.1.1 用户管理

| 功能 | 状态 | 说明 |
|------|------|------|
| 用户登录 | ✅ 已实现 | POST `/oa/auth/login`，返回 JWT Token |
| 获取当前用户信息 | ✅ 已实现 | GET `/oa/auth/user-info` |
| 分页查询用户 | ✅ 已实现 | GET `/oa/system/user/page`，支持按用户名/姓名/部门/状态筛选 |
| 新增用户 | ✅ 已实现 | POST `/oa/system/user`，密码 BCrypt 加密 |
| 更新用户 | ✅ 已实现 | PUT `/oa/system/user` |
| 删除用户 | ✅ 已实现 | DELETE `/oa/system/user/{id}`，逻辑删除 |
| 重置密码 | ✅ 已实现 | PUT `/oa/system/user/{id}/reset-pwd`，重置为 123456 |

#### 4.1.2 角色管理（待实现）

- 角色 CRUD
- 角色-菜单权限分配
- 用户-角色关联

#### 4.1.3 部门管理（待实现）

- 部门树查询
- 部门 CRUD
- 部门用户统计

#### 4.1.4 菜单管理（待实现）

- 菜单树查询
- 菜单 CRUD（目录/菜单/按钮三种类型）
- 前端动态路由集成

### 4.2 模块2：考勤管理（待实现）

| 功能 | 说明 |
|------|------|
| 每日打卡 | 签到/签退，记录时间+地点 |
| 考勤记录查询 | 按月份查看打卡历史，状态（正常/迟到/早退/缺勤） |
| 请假申请 | 年假/事假/病假/调休，选择日期范围 |
| 请假审批 | 上级审批，多级审批流程 |
| 考勤统计 | 月度出勤率、迟到次数等图表 |

**关键业务规则**：
- 迟到判定：签到时间 > 9:00
- 早退判定：签退时间 < 18:00
- 缺勤判定：工作日无打卡记录
- 请假天数 ≤ 剩余年假天数（年假）
- 请假日期不能与已批准的请假重叠

### 4.3 模块3：审批中心（待实现）

| 功能 | 说明 |
|------|------|
| 审批模板管理 | 定义审批流程模板（如"请假审批需2级"） |
| 发起审批 | 选择模板 → 填写表单 → 提交 |
| 待审批列表 | 当前用户需要审批的事项 |
| 已审批列表 | 审批历史记录 |
| 我的申请 | 我发起的审批及其状态 |

**审批流程引擎核心逻辑**：

```java
// 多级审批流转伪代码
public void approve(Long instanceId, int result, String comment) {
    ApprovalInstance instance = getById(instanceId);
    // 1. 记录当前级审批意见
    saveRecord(instanceId, instance.getCurrentLevel(), currentUser, result, comment);
    // 2. 如果驳回 → 整个审批结束
    if (result == REJECTED) {
        instance.setStatus(REJECTED);
        updateById(instance);
        return;
    }
    // 3. 如果同意且还有下一级 → 流转到下一级
    if (instance.getCurrentLevel() < instance.getTotalLevels()) {
        instance.setCurrentLevel(instance.getCurrentLevel() + 1);
    } else {
        // 4. 所有级别通过 → 审批完成
        instance.setStatus(APPROVED);
    }
    updateById(instance);
}
```

### 4.4 模块4：公告通知（待实现）

| 功能 | 说明 |
|------|------|
| 发布公告 | 富文本编辑器，支持置顶、定时发布 |
| 公告列表 | 分页查询，按类型筛选 |
| 公告详情 | 查看内容，记录阅读次数 |
| 草稿管理 | 暂存未发布的公告 |

### 4.5 模块5：日程管理（待实现）

| 功能 | 说明 |
|------|------|
| 新增日程 | 个人/部门/会议三种类型 |
| 日程列表 | 按日/周/月视图展示 |
| 日程提醒 | 到达时间后状态变更 |
| 会议管理 | 邀请参与人，地点预约 |

### 4.6 模块6：报销管理（待实现）

| 功能 | 说明 |
|------|------|
| 报销申请 | 选择类型 → 填写金额 → 上传发票附件 |
| 报销审批 | 多级审批，查看附件 |
| 报销记录 | 按状态筛选，查看审批意见 |
| 费用统计 | 按类型/月份统计报销金额 |

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
| `att_record` | 考勤记录 | user_id, attendance_date, sign_in_time, sign_out_time, status |
| `att_leave_request` | 请假申请 | user_id, leave_type, start_date, end_date, days, status |

#### 审批模块（3 表）

| 表名 | 说明 | 关键字段 |
|------|------|---------|
| `appr_template` | 审批模板 | template_name, template_code, approval_levels |
| `appr_instance` | 审批实例 | template_id, applicant_id, current_level, status |
| `appr_record` | 审批记录 | instance_id, level, approver_id, result |

#### 其他模块（3 表）

| 表名 | 说明 |
|------|------|
| `sys_notice` | 公告通知 |
| `sch_schedule` | 日程管理 |
| `exp_request` | 报销申请 |

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

### 6.4 待实现接口（规划）

#### 角色管理 `/oa/system/role`

```
GET    /oa/system/role/list     ← 角色列表
GET    /oa/system/role/{id}     ← 角色详情
POST   /oa/system/role          ← 新增角色
PUT    /oa/system/role          ← 更新角色
DELETE /oa/system/role/{id}     ← 删除角色
PUT    /oa/system/role/{id}/menus ← 分配菜单权限
```

#### 考勤管理 `/oa/attendance`

```
POST   /oa/attendance/sign-in   ← 签到
POST   /oa/attendance/sign-out  ← 签退
GET    /oa/attendance/records   ← 考勤记录（按月份）
POST   /oa/attendance/leave     ← 请假申请
GET    /oa/attendance/leave/list ← 请假列表
PUT    /oa/attendance/leave/{id}/approve ← 审批请假
```

#### 审批中心 `/oa/approval`

```
POST   /oa/approval/start       ← 发起审批
GET    /oa/approval/todo        ← 待审批列表
GET    /oa/approval/done        ← 已审批列表
GET    /oa/approval/my          ← 我的申请
POST   /oa/approval/{id}/approve ← 审批操作（同意/驳回）
```

#### 公告通知 `/oa/notice`

```
GET    /oa/notice/page          ← 公告列表（分页）
GET    /oa/notice/{id}          ← 公告详情
POST   /oa/notice               ← 发布公告
PUT    /oa/notice               ← 编辑公告
DELETE /oa/notice/{id}          ← 删除公告
```

#### 日程管理 `/oa/schedule`

```
GET    /oa/schedule/list        ← 日程列表（按日期范围）
POST   /oa/schedule             ← 新增日程
PUT    /oa/schedule             ← 更新日程
DELETE /oa/schedule/{id}        ← 删除日程
```

#### 报销管理 `/oa/expense`

```
GET    /oa/expense/page         ← 报销列表
POST   /oa/expense              ← 提交报销
PUT    /oa/expense/{id}/approve ← 审批报销
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

- ⚠️ **所有请求均已放行**（`anyRequest().permitAll()`）—— 开发初期策略
- 下一步：开启 JWT 认证 + 接口级别权限控制

---

## 8. 开发路线图

### Phase 1：基础框架（✅ 已完成）

- [x] 项目骨架搭建（Maven + Spring Boot）
- [x] 数据库表设计 + 初始化脚本
- [x] 通用组件（Result、PageResult、BaseEntity、异常处理）
- [x] Spring Security + JWT 基础配置
- [x] 用户登录 + 用户管理 CRUD
- [x] H2 内置数据库（开箱即用运行）

### Phase 2：系统管理完善（优先级：高 🔴）

- [ ] 完成 Spring Security + JWT 认证集成（目前只是框架）
- [ ] 角色管理 CRUD + 菜单树
- [ ] 部门管理 CRUD + 部门树
- [ ] 菜单管理 + 动态路由数据源
- [ ] 用户-角色分配、角色-菜单分配
- [ ] 前端项目初始化（Vue 3 + Element Plus）对接接口

### Phase 3：核心业务模块（优先级：中 🟡）

- [ ] 考勤打卡（签到/签退）
- [ ] 请假申请 + 审批流程
- [ ] 审批中心（模板 + 实例 + 多级流转）
- [ ] 公告通知（发布/列表/置顶）

### Phase 4：扩展模块（优先级：低 🟢）

- [ ] 日程管理
- [ ] 费用报销
- [ ] 数据看板（ECharts 统计图表）
- [ ] 文件上传（头像/附件）
- [ ] 操作日志记录

### Phase 5：上线准备

- [ ] 切换到 MySQL
- [ ] 单元测试 + 集成测试
- [ ] 性能优化（索引/缓存）
- [ ] Docker 部署脚本
- [ ] 接口文档完善

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

### 10.2 切换到 MySQL

1. 安装 MySQL 8.0
2. 创建数据库：`CREATE DATABASE oa_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`
3. 导入建表脚本：`mysql -u root -p oa_system < sql/schema-mysql.sql`
4. 修改 `application-dev.yml` 中的数据源配置
5. 重启应用

### 10.3 打包部署

```bash
# 打包为可执行 JAR
mvn clean package -DskipTests

# 运行
java -jar target/oa-system.jar --spring.profiles.active=prod
```

### 10.4 CLion 运行配置

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

**Q: 如何查看 H2 数据库内容？**
A: 启动后访问 `http://localhost:8080/oa/h2-console`，JDBC URL 填 `jdbc:h2:file:./data/oa-system`

**Q: 如何添加新模块？**
A: 在 `com.oasystem` 下新建包（如 `warehouse`）→ 添加 entity/mapper/service/controller 子包 → 在 `@MapperScan` 覆盖范围内即可自动扫描。

**Q: MyBatis XML 放哪里？**
A: 放在 `src/main/resources/mapper/` 下，MyBatis-Plus 会自动扫描。

---

> **本文档面向 AI 辅助开发场景编写。后续开发中，任何智能体读取此文档后应能理解项目全貌、当前进度和待办事项，从而精确辅助编码。**
