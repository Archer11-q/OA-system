# OA System — 后端

本仓库包含 OA 办公自动化系统后端（Spring Boot + MyBatis-Plus + JWT）的源码与文档。

快速运行（开发环境，内置 H2）：

```powershell
cd D:\CLion\oa-system
D:\CLion\tools\apache-maven-3.9.16\bin\mvn spring-boot:run
```

访问：
- 应用根路径: http://localhost:8080/oa
- API 文档（Knife4j/Swagger）： http://localhost:8080/oa/doc.html
- H2 控制台： http://localhost:8080/oa/h2-console （JDBC URL: jdbc:h2:file:./data/oa-system，用户: sa，密码: 空）

项目当前状态（与 `doc/DESIGN.md` 保持一致；若需要其它视图请告知）：

- 模块1 - 系统管理：基础已实现（用户登录、用户 CRUD、菜单/部门/角色的核心接口与部分校验已完成）。
- 模块2 - 考勤：已实现（DEV-08/18），签到/签退自动判定迟到早退+工时计算+月度汇总报表+每日状态明细。
- 模块3 - 审批中心：多级审批引擎已实现（DEV-16），含审批人配置/权限验证/模板管理。
- 模块4 - 公告通知：列表/详情/发布/编辑/删除（管理员权限）已实现。
- 模块5 - 日程管理：基础 CRUD 已实现（DEV-15），日/周/月视图与提醒为后续迭代。
- 模块6 - 报销管理：基础 CRUD 已实现（DEV-17），提交/列表/修改/删除 + 统计汇总，审批集成待后续。

主要开发/迭代记录简要（最新在 `doc/DESIGN.md` 中）：

 - DEV-01..DEV-03：项目骨架、JWT + Security、基础文档与健康检查
 - DEV-04..DEV-07：角色/菜单/部门 管理与权限初步实现
 - DEV-06：菜单管理 CRUD（已完成）
 - DEV-08：考勤模块基础实现（签到/签退/请假）
 - DEV-09：公告模块基础实现
 - DEV-10：审批中心骨架（模板/实例/记录）
 - DEV-11..DEV-14：RBAC 完善、dataScope 支持、菜单 perms 唯一性校验、菜单删除安全性

说明与注意：
- `doc/DESIGN.md` 是主设计与迭代记录文档，后续请以该文件为权威进度记录。README 只保留高层摘要与启动说明。
- 你打算把 `DESIGN.md` 给其他智能体使用时，我会保持该文件为单一真实来源（同步更新实现/未实现项），并把 README 保持为简短引用。

详细技术设计、接口说明和数据字典请在 `doc/` 目录下查看或告诉我需要把哪些条目同步进 README。

若你同意，我将把 `README.md` 清理为当前内容（已完成），并把变更提交到仓库。
