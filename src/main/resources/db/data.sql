-- ==============================================
-- OA System - 初始数据
-- 预设：管理员账号、基础部门、基础角色、菜单
-- ==============================================

-- ---------- 部门 ----------
INSERT INTO sys_dept (id, parent_id, dept_name, leader_id, sort, status, ancestors) VALUES
(1, 0, '总公司',    NULL, 0, 1, '0'),
(2, 1, '技术部',   NULL, 1, 1, '0,1'),
(3, 1, '人事部',   NULL, 2, 1, '0,1'),
(4, 1, '财务部',   NULL, 3, 1, '0,1'),
(5, 1, '市场部',   NULL, 4, 1, '0,1');

-- ---------- 用户（密码均为 123456 的 BCrypt 加密值） ----------
INSERT INTO sys_user (id, username, password, real_name, employee_no, gender, phone, email, dept_id, status) VALUES
(1, 'admin',    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'E001', 1, '13800000000', 'admin@oa.com', 1, 1),
(2, 'zhangsan', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张三',       'E002', 1, '13800000001', 'zhangsan@oa.com', 2, 1),
(3, 'lisi',     '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李四',       'E003', 0, '13800000002', 'lisi@oa.com', 3, 1);

-- ---------- 角色 ----------
INSERT INTO sys_role (id, role_name, role_code, description, sort, status, data_scope) VALUES
(1, '超级管理员', 'ROLE_ADMIN',   '拥有所有权限',        1, 1, 1),
(2, '部门经理',   'ROLE_MANAGER', '管理部门事务',        2, 1, 2),
(3, '普通员工',   'ROLE_EMPLOYEE','查看与操作个人事务',   3, 1, 4);

-- ---------- 菜单 ----------
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perms, path, component, icon, sort, visible, is_frame) VALUES
-- 目录
(1,  0, '系统管理',  0, NULL,      '/system',     NULL,        'system',   1, 1, 0),
(2,  1, '用户管理',  1, 'system:user:list',   '/system/user',   'system/user/index',   'user',     1, 1, 0),
(3,  1, '角色管理',  1, 'system:role:list',   '/system/role',   'system/role/index',   'peoples',  2, 1, 0),
(4,  1, '菜单管理',  1, 'system:menu:list',   '/system/menu',   'system/menu/index',   'tree-table', 3, 1, 0),
(5,  1, '部门管理',  1, 'system:dept:list',   '/system/dept',   'system/dept/index',   'tree',     4, 1, 0),
(10, 0, '考勤管理',  0, NULL,      '/attendance', NULL,        'time-range', 2, 1, 0),
(11, 10,'考勤记录',  1, 'attendance:record:list', '/attendance/record', 'attendance/record/index', 'form', 1, 1, 0),
(12, 10,'请假管理',  1, 'attendance:leave:list',  '/attendance/leave',  'attendance/leave/index',  'edit', 2, 1, 0),
(20, 0, '审批中心',  0, NULL,      '/approval',   NULL,        'guide',    3, 1, 0),
(21, 20,'发起审批',  1, 'approval:start',     '/approval/start',    'approval/start/index',    'start',   1, 1, 0),
(22, 20,'我的审批',  1, 'approval:my-approve', '/approval/my',       'approval/my/index',       'skill',   2, 1, 0),
(23, 20,'审批待办',  1, 'approval:todo',       '/approval/todo',     'approval/todo/index',     'example', 3, 1, 0),
(30, 0, '公告通知',  0, NULL,      '/notice',     NULL,        'message',  4, 1, 0),
(31, 30,'公告列表',  1, 'notice:list', '/notice/list', 'notice/list/index', 'list',       1, 1, 0),
(40, 0, '日程管理',  0, NULL,      '/schedule',   NULL,        'calendar', 5, 1, 0),
(41, 40,'我的日程',  1, 'schedule:list', '/schedule/list', 'schedule/list/index', 'date',      1, 1, 0),
(50, 0, '报销管理',  0, NULL,      '/expense',    NULL,        'money',    6, 1, 0),
(51, 50,'我的报销',  1, 'expense:list', '/expense/list', 'expense/list/index', 'pay',       1, 1, 0);

-- ---------- 用户-角色关联 ----------
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),  -- admin => 超级管理员
(2, 2),  -- zhangsan => 部门经理
(3, 3);  -- lisi => 普通员工

-- ---------- 角色-菜单关联（admin拥有全部菜单） ----------
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu;

-- ---------- 审批模板 ----------
-- 请假审批模板：2级审批 — 部门负责人 → 管理员
INSERT INTO appr_template (id, template_name, template_code, description, approval_levels, status, approvers_config) VALUES
(1, '请假审批', 'LEAVE_APPROVAL', '员工请假审批流程：部门负责人审批 → 管理员终审', 2, 1,
 '[{"level":1,"type":"DEPT_LEADER"},{"level":2,"type":"ROLE","value":"ROLE_ADMIN"}]');
