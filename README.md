<p align="center">
  <h1 align="center">🏢 OA System</h1>
  <p align="center"><strong>全栈企业级办公自动化系统</strong></p>
  <p align="center">Vue 3 + Element Plus · Spring Boot 3.2 · MyBatis-Plus · Spring Security + JWT</p>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/JDK-21_LTS-orange?logo=openjdk" alt="JDK 21">
  <img src="https://img.shields.io/badge/Spring_Boot-3.2.7-brightgreen?logo=springboot" alt="Spring Boot 3.2.7">
  <img src="https://img.shields.io/badge/Vue-3.x-4FC08D?logo=vuedotjs" alt="Vue 3">
  <img src="https://img.shields.io/badge/MySQL-8.0-blue?logo=mysql" alt="MySQL 8.0">
  <img src="https://img.shields.io/badge/Docker-Ready-2496ED?logo=docker" alt="Docker">
  <img src="https://img.shields.io/badge/License-MIT-green" alt="License">
</p>

---

## 📖 项目简介

OA System 是一套**前后端分离**的企业级办公自动化系统，覆盖企业日常行政管理的核心场景：用户与权限管理、考勤打卡、多级审批流程、公告通知、日程管理、费用报销，并配备数据看板与操作日志。

适合作为：**课程设计 / 毕业设计** 交付，或 **全栈开发学习** 参考项目。

### ✨ 核心特性

| 模块 | 核心能力 |
|------|---------|
| 🔐 **系统管理** | 用户/角色/菜单/部门 CRUD + RBAC 权限模型 + 数据权限（dataScope） |
| 📋 **考勤管理** | 签到签退（自动判定迟到早退）+ 月度汇总 + 每日明细 + 请假审批联动 |
| ✅ **审批中心** | 多级审批引擎（DEPT_LEADER / ROLE / USER 三种审批人）+ 模板管理 + 撤回 + 业务回调 |
| 📢 **公告通知** | 发布/编辑/删除 + 分页列表 + 详情查看 |
| 📅 **日程管理** | CRUD + 日期范围查询 + 参与人 + 个人权限控制 |
| 💰 **报销管理** | CRUD + 按状态过滤 + 金额统计 + 审批联动 |
| 📊 **数据看板** | 系统概览 + 考勤趋势 + 审批/报销分布（ECharts 图表） |
| 📝 **操作日志** | AOP 自动记录（@Log 注解）+ 分页查询 + 定时清理 |
| 📎 **文件上传** | 通用上传 + 头像上传 + 类型校验 + UUID 防冲突 |

---

## 🛠 技术栈

| 层次 | 技术 | 说明 |
|------|------|------|
| **后端框架** | Spring Boot 3.2.7 | 内嵌 Tomcat，开箱即用 |
| **ORM** | MyBatis-Plus 3.5.6 | Lambda 查询、分页插件、自动填充 |
| **安全** | Spring Security 6.x + JWT (jjwt 0.12) | 无状态认证、RBAC 权限、BCrypt 加密 |
| **数据库** | MySQL 8.0（生产）/ H2（开发） | H2 零配置启动，开箱即用 |
| **API 文档** | Knife4j 4.3 (Swagger 增强) | 接口文档自动生成 + 在线调试 |
| **工具库** | Hutool 5.8 + Lombok | 集合/日期/加密工具 + 代码简化 |
| **前端框架** | Vue 3 + Vite | Composition API + 快速构建 |
| **UI 组件** | Element Plus | 企业级桌面端组件库 |
| **状态管理** | Pinia | Vue 3 官方推荐 |
| **图表** | ECharts 5.x | 数据看板可视化 |
| **构建部署** | Maven + Docker + Docker Compose | 多阶段构建，一键部署 |

---

## 🚀 快速开始

### 前置要求

- **JDK 21+**
- **Maven 3.9+**
- **Node.js 18+**（前端）
- **Docker Desktop**（可选，用于容器化部署）

### 1. 后端启动（开发环境，内置 H2 数据库）

```bash
# 克隆项目
git clone <your-repo-url>
cd oa-system

# 启动 Spring Boot（H2 自动初始化，无需安装数据库）
mvn spring-boot:run
```

启动后访问：

| 地址 | 说明 |
|------|------|
| http://localhost:8080/oa | 应用根路径 |
| http://localhost:8080/oa/doc.html | Knife4j API 文档（在线调试） |
| http://localhost:8080/oa/h2-console | H2 控制台（JDBC URL: `jdbc:h2:file:./data/oa-system`，用户 `sa`，密码空） |

### 2. 前端启动

```bash
cd frontend
npm install
npm run dev
```

访问 http://localhost:5173（已配置代理到后端 `localhost:8080`）

> **默认账号**：`admin` / `123456`

### 3. Docker 一键部署（MySQL + 应用）

```bash
docker compose up -d    # Docker Desktop 使用 "docker compose"（空格）

# 查看日志
docker compose logs -f oa-app

# 停止
docker compose down
```

> **注意**：新版 Docker Desktop 使用 `docker compose`（空格），旧版使用 `docker-compose`（连字符）。

---

## 📂 项目结构

```
oa-system/
├── src/main/java/com/oasystem/
│   ├── system/          # 模块1：系统管理（用户/角色/菜单/部门）
│   ├── attendance/      # 模块2：考勤管理（签到/请假/统计）
│   ├── approval/        # 模块3：审批中心（多级引擎/模板/撤回）
│   ├── notice/          # 模块4：公告通知
│   ├── schedule/        # 模块5：日程管理
│   ├── expense/         # 模块6：报销管理
│   ├── log/             # 操作日志（AOP）
│   ├── config/          # Spring Security / CORS / MyBatis-Plus
│   └── security/        # JWT 认证（Token 生成/解析/过滤器）
├── src/main/resources/
│   ├── application-dev.yml       # 开发环境（H2）
│   ├── application-prod.yml      # 生产环境（MySQL + HikariCP）
│   ├── db/schema-h2.sql          # H2 建表脚本
│   ├── db/data.sql               # 初始数据
│   └── mapper/                   # MyBatis XML 映射
├── frontend/                     # Vue 3 前端项目
│   └── src/
│       ├── api/                  # API 模块（8个业务模块）
│       ├── views/                # 页面视图（10个页面）
│       ├── stores/               # Pinia 状态管理
│       ├── router/               # 路由 + 守卫
│       └── layout/               # 主布局（侧边栏 + Header）
├── sql/schema-mysql.sql          # MySQL 完整建表脚本
├── Dockerfile                    # 多阶段构建
├── docker-compose.yml            # Docker Compose（MySQL + App）
├── doc/DESIGN.md                 # 详细设计文档（含 DEV 迭代记录）
└── pom.xml
```

---

## 🔒 安全设计

- **认证**：JWT 无状态 Token，24 小时有效期，前端路由守卫解析 `exp` 判定过期
- **授权**：RBAC 模型 — 用户 → 角色 → 菜单/权限，接口级 `@PreAuthorize` 控制
- **密码**：BCrypt 加密存储，默认密码 `123456`
- **数据权限**：`dataScope` 字段控制（全部数据 / 本部门 / 仅本人）
- **401/403**：统一 JSON 响应，Axios 拦截器自动跳转登录页

---

## 📊 开发进度

| 阶段 | 内容 | 状态 |
|------|------|:--:|
| Phase 1 | 基础框架（Maven + Spring Boot + H2 + 通用组件） | ✅ |
| Phase 2 | 系统管理完善（JWT + RBAC + 用户/角色/菜单/部门） | ✅ |
| Phase 3 | 核心业务（考勤 + 请假 + 审批引擎 + 公告） | ✅ |
| Phase 4 | 扩展模块（日程 + 报销 + 操作日志 + 文件上传 + 看板） | ✅ |
| Phase 5 | 上线准备（MySQL 切换 + 单元测试 + Docker） | ✅ |
| **测试** | JUnit 5 + Mockito，覆盖 Approval / Attendance / User 核心 Service | ✅ 33 用例 |

> 详细的 DEV 迭代日志、API 接口文档、数据库设计，请查看 **[doc/DESIGN.md](doc/DESIGN.md)**。

---

## ⚠️ 注意事项

- **H2 数据库修改**：修改 `schema-h2.sql` 后，需**手动删除** `data/oa-system.mv.db` 文件再重启，`CREATE TABLE IF NOT EXISTS` 不会更新已有表结构。
- **Docker 命令**：Docker Desktop 使用 `docker compose`（空格），不是旧的 `docker-compose`（连字符）。
- **生产部署**：切换到 MySQL 请使用 `application-prod.yml`（含 HikariCP 连接池 + 文件日志），先执行 `sql/schema-mysql.sql` 建表。
- **配置文件**：启动前请修改 `application-prod.yml` 中的数据库用户名/密码。

---

## 🔮 后续可扩展

| 优先级 | 方向 | 说明 |
|:--:|------|------|
| ✅ | 并行审批 | 同一级别多人审批，任一人同意即可通过（DEV-30） |
| 🟡 | 日程日历视图 | FullCalendar 对接，日/周/月视图 |
| 🟡 | 前端优化 | 路由懒加载、组件缓存、打包优化 |
| 🟡 | 接口文档 | 补充 Knife4j 注解细节 |

---

## 📄 License

MIT License

---

> **详细设计文档**：[doc/DESIGN.md](doc/DESIGN.md) — 含完整 API 接口、数据库设计、DEV 迭代记录、部署说明。
