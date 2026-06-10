-- ==============================================
-- OA System - H2 数据库初始化建表脚本
-- 与 MySQL 兼容（MODE=MySQL）
-- ==============================================

-- ---------- 系统管理模块 ----------

-- 部门表
CREATE TABLE IF NOT EXISTS sys_dept (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id   BIGINT       DEFAULT 0   COMMENT '父部门ID',
    dept_name   VARCHAR(64)  NOT NULL    COMMENT '部门名称',
    leader_id   BIGINT       DEFAULT NULL COMMENT '负责人ID',
    sort        INT          DEFAULT 0   COMMENT '排序',
    status      TINYINT      DEFAULT 1   COMMENT '状态(0=停用,1=正常)',
    ancestors   VARCHAR(500) DEFAULT ''  COMMENT '祖级列表',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    create_by   BIGINT       DEFAULT NULL,
    update_by   BIGINT       DEFAULT NULL,
    deleted     TINYINT      DEFAULT 0   COMMENT '逻辑删除(0=正常,1=删除)',
    remark      VARCHAR(500) DEFAULT NULL
);

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(32)  NOT NULL UNIQUE COMMENT '用户名',
    password        VARCHAR(128) NOT NULL    COMMENT '密码(BCrypt加密)',
    real_name       VARCHAR(32)  DEFAULT NULL COMMENT '真实姓名',
    employee_no     VARCHAR(32)  DEFAULT NULL COMMENT '工号',
    gender          TINYINT      DEFAULT 1   COMMENT '性别(0=女,1=男)',
    phone           VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    email           VARCHAR(64)  DEFAULT NULL COMMENT '邮箱',
    avatar          VARCHAR(256) DEFAULT NULL COMMENT '头像URL',
    dept_id         BIGINT       DEFAULT NULL COMMENT '所属部门ID',
    entry_date      DATE         DEFAULT NULL COMMENT '入职日期',
    status          TINYINT      DEFAULT 1   COMMENT '状态(0=禁用,1=启用)',
    last_login_time DATETIME     DEFAULT NULL COMMENT '最后登录时间',
    last_login_ip   VARCHAR(64)  DEFAULT NULL COMMENT '最后登录IP',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    create_by       BIGINT       DEFAULT NULL,
    update_by       BIGINT       DEFAULT NULL,
    deleted         TINYINT      DEFAULT 0,
    remark          VARCHAR(500) DEFAULT NULL
);

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name   VARCHAR(32)  NOT NULL COMMENT '角色名称',
    role_code   VARCHAR(32)  NOT NULL UNIQUE COMMENT '角色编码',
    description VARCHAR(256) DEFAULT NULL COMMENT '角色描述',
    sort        INT          DEFAULT 0   COMMENT '排序',
    status      TINYINT      DEFAULT 1   COMMENT '状态',
    data_scope  TINYINT      DEFAULT 4   COMMENT '数据权限范围(1全部 2本部门及子部门 3本部门 4本人)',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    create_by   BIGINT       DEFAULT NULL,
    update_by   BIGINT       DEFAULT NULL,
    deleted     TINYINT      DEFAULT 0,
    remark      VARCHAR(500) DEFAULT NULL
);

-- 菜单/权限表
CREATE TABLE IF NOT EXISTS sys_menu (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id   BIGINT       DEFAULT 0   COMMENT '父菜单ID',
    menu_name   VARCHAR(64)  NOT NULL    COMMENT '菜单名称',
    menu_type   TINYINT      DEFAULT 1   COMMENT '菜单类型(0=目录,1=菜单,2=按钮)',
    perms       VARCHAR(128) DEFAULT NULL COMMENT '权限标识',
    path        VARCHAR(256) DEFAULT NULL COMMENT '路由路径',
    component   VARCHAR(256) DEFAULT NULL COMMENT '前端组件',
    icon        VARCHAR(64)  DEFAULT NULL COMMENT '图标',
    sort        INT          DEFAULT 0   COMMENT '排序',
    visible     TINYINT      DEFAULT 1   COMMENT '是否显示(0=隐藏,1=显示)',
    is_frame    TINYINT      DEFAULT 0   COMMENT '是否外链',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    create_by   BIGINT       DEFAULT NULL,
    update_by   BIGINT       DEFAULT NULL,
    deleted     TINYINT      DEFAULT 0,
    remark      VARCHAR(500) DEFAULT NULL
);

-- 用户-角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (user_id, role_id)
);

-- 角色-菜单关联表
CREATE TABLE IF NOT EXISTS sys_role_menu (
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (role_id, menu_id)
);

-- ---------- 考勤模块 ----------

-- 考勤记录表
CREATE TABLE IF NOT EXISTS att_record (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id           BIGINT       NOT NULL    COMMENT '用户ID',
    attendance_date   DATE         NOT NULL    COMMENT '考勤日期',
    sign_in_time      TIME         DEFAULT NULL COMMENT '签到时间',
    sign_out_time     TIME         DEFAULT NULL COMMENT '签退时间',
    status            VARCHAR(16)  DEFAULT 'ABSENT' COMMENT '签到状态',
    sign_in_type      TINYINT      DEFAULT 1   COMMENT '签到类型(1=正常,2=外勤,3=补卡)',
    sign_out_type     TINYINT      DEFAULT 1   COMMENT '签退类型',
    work_hours        FLOAT        DEFAULT 0   COMMENT '工作时长(小时)',
    sign_in_location  VARCHAR(256) DEFAULT NULL COMMENT '签到地点',
    sign_out_location VARCHAR(256) DEFAULT NULL COMMENT '签退地点',
    create_time       DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time       DATETIME     DEFAULT CURRENT_TIMESTAMP,
    create_by         BIGINT       DEFAULT NULL,
    update_by         BIGINT       DEFAULT NULL,
    deleted           TINYINT      DEFAULT 0,
    remark            VARCHAR(500) DEFAULT NULL
);

-- 请假申请表
CREATE TABLE IF NOT EXISTS att_leave_request (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id          BIGINT        NOT NULL    COMMENT '申请人ID',
    leave_type       TINYINT       NOT NULL    COMMENT '请假类型(1年假 2事假 3病假 4调休)',
    start_date       DATE          NOT NULL    COMMENT '开始日期',
    end_date         DATE          NOT NULL    COMMENT '结束日期',
    days             FLOAT         NOT NULL    COMMENT '请假天数',
    reason           VARCHAR(512)  DEFAULT NULL COMMENT '请假原因',
    status           TINYINT       DEFAULT 0   COMMENT '状态(0审批中 1已通过 2已驳回 3已撤回)',
    approval_comment VARCHAR(256)  DEFAULT NULL COMMENT '审批意见',
    approver_id      BIGINT        DEFAULT NULL COMMENT '审批人ID',
    approval_time    DATETIME      DEFAULT NULL COMMENT '审批时间',
    create_time      DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME      DEFAULT CURRENT_TIMESTAMP,
    create_by        BIGINT        DEFAULT NULL,
    update_by        BIGINT        DEFAULT NULL,
    deleted          TINYINT       DEFAULT 0,
    remark           VARCHAR(500)  DEFAULT NULL
);

-- ---------- 审批模块 ----------

-- 审批模板表
CREATE TABLE IF NOT EXISTS appr_template (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_name   VARCHAR(64)  NOT NULL    COMMENT '模板名称',
    template_code   VARCHAR(32)  NOT NULL UNIQUE COMMENT '模板编码',
    description     VARCHAR(256) DEFAULT NULL COMMENT '模板描述',
    approval_levels TINYINT      DEFAULT 1   COMMENT '审批级数',
    status           TINYINT      DEFAULT 1   COMMENT '状态',
    approvers_config TEXT         DEFAULT NULL COMMENT '审批人配置(JSON)',
    create_time      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    create_by        BIGINT       DEFAULT NULL,
    update_by        BIGINT       DEFAULT NULL,
    deleted          TINYINT      DEFAULT 0,
    remark           VARCHAR(500) DEFAULT NULL
);

-- 审批实例表
CREATE TABLE IF NOT EXISTS appr_instance (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id   BIGINT        NOT NULL    COMMENT '审批模板ID',
    applicant_id  BIGINT        NOT NULL    COMMENT '申请人ID',
    title         VARCHAR(128)  NOT NULL    COMMENT '审批标题',
    content       TEXT          DEFAULT NULL COMMENT '审批内容(JSON表单数据)',
    total_levels  TINYINT       DEFAULT 1   COMMENT '总审批级数',
    current_level TINYINT       DEFAULT 1   COMMENT '当前审批级别',
    status        TINYINT       DEFAULT 0   COMMENT '状态(0审批中 1已通过 2已驳回 3已撤回)',
    approvers_snapshot TEXT     DEFAULT NULL COMMENT '审批人快照(JSON)',
    finish_time   DATETIME      DEFAULT NULL COMMENT '完成时间',
    create_time   DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME      DEFAULT CURRENT_TIMESTAMP,
    create_by     BIGINT        DEFAULT NULL,
    update_by     BIGINT        DEFAULT NULL,
    deleted       TINYINT       DEFAULT 0,
    remark        VARCHAR(500)  DEFAULT NULL
);

-- 审批记录表（每一级的审批意见）
CREATE TABLE IF NOT EXISTS appr_record (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    instance_id   BIGINT       NOT NULL    COMMENT '审批实例ID',
    level         TINYINT      NOT NULL    COMMENT '第几级审批',
    approver_id   BIGINT       NOT NULL    COMMENT '审批人ID',
    result        TINYINT      DEFAULT 0   COMMENT '审批结果(0待审批 1同意 2驳回)',
    comment       VARCHAR(256) DEFAULT NULL COMMENT '审批意见',
    approval_time DATETIME     DEFAULT NULL COMMENT '审批时间',
    create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    create_by     BIGINT       DEFAULT NULL,
    update_by     BIGINT       DEFAULT NULL,
    deleted       TINYINT      DEFAULT 0,
    remark        VARCHAR(500) DEFAULT NULL
);

-- ---------- 公告模块 ----------

-- 公告/通知表
CREATE TABLE IF NOT EXISTS sys_notice (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    title        VARCHAR(128)  NOT NULL    COMMENT '公告标题',
    content      TEXT          DEFAULT NULL COMMENT '公告内容(富文本)',
    notice_type  TINYINT       DEFAULT 1   COMMENT '公告类型(1通知 2公告 3制度)',
    status       TINYINT       DEFAULT 0   COMMENT '发布状态(0草稿 1已发布)',
    is_top       TINYINT       DEFAULT 0   COMMENT '是否置顶',
    publish_time DATETIME      DEFAULT NULL COMMENT '发布时间',
    publisher_id BIGINT        DEFAULT NULL COMMENT '发布人ID',
    view_count   INT           DEFAULT 0   COMMENT '查看次数',
    create_time  DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME      DEFAULT CURRENT_TIMESTAMP,
    create_by    BIGINT        DEFAULT NULL,
    update_by    BIGINT        DEFAULT NULL,
    deleted      TINYINT       DEFAULT 0,
    remark       VARCHAR(500)  DEFAULT NULL
);

-- ---------- 日程模块 ----------

-- 日程表
CREATE TABLE IF NOT EXISTS sch_schedule (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    title          VARCHAR(128)  NOT NULL    COMMENT '日程标题',
    content        VARCHAR(1024) DEFAULT NULL COMMENT '日程内容',
    start_time     DATETIME      NOT NULL    COMMENT '开始时间',
    end_time       DATETIME      NOT NULL    COMMENT '结束时间',
    schedule_type  TINYINT       DEFAULT 1   COMMENT '类型(1个人 2部门 3会议)',
    priority       TINYINT       DEFAULT 1   COMMENT '重要程度(1普通 2重要 3紧急)',
    location       VARCHAR(256)  DEFAULT NULL COMMENT '地点',
    creator_id     BIGINT        NOT NULL    COMMENT '创建人ID',
    participant_ids TEXT         DEFAULT NULL COMMENT '参与人ID列表(JSON数组)',
    status         TINYINT       DEFAULT 0   COMMENT '状态(0未开始 1进行中 2已完成 3已取消)',
    create_time    DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time    DATETIME      DEFAULT CURRENT_TIMESTAMP,
    create_by      BIGINT        DEFAULT NULL,
    update_by      BIGINT        DEFAULT NULL,
    deleted        TINYINT       DEFAULT 0,
    remark         VARCHAR(500)  DEFAULT NULL
);

-- ---------- 报销模块 ----------

-- 报销申请表
CREATE TABLE IF NOT EXISTS exp_request (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id          BIGINT         NOT NULL    COMMENT '申请人ID',
    title            VARCHAR(128)   NOT NULL    COMMENT '报销标题',
    expense_type     TINYINT        NOT NULL    COMMENT '报销类型(1差旅 2办公 3招待 4交通 5其他)',
    amount           DECIMAL(12,2)  NOT NULL    COMMENT '报销金额',
    description      VARCHAR(512)   DEFAULT NULL COMMENT '报销说明',
    attachments      TEXT           DEFAULT NULL COMMENT '附件URL列表(JSON)',
    status           TINYINT        DEFAULT 0   COMMENT '状态(0审批中 1已通过 2已驳回 3已撤回)',
    approver_id      BIGINT         DEFAULT NULL COMMENT '审批人ID',
    approval_comment VARCHAR(256)   DEFAULT NULL COMMENT '审批意见',
    approval_time    DATETIME       DEFAULT NULL COMMENT '审批时间',
    create_time      DATETIME       DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME       DEFAULT CURRENT_TIMESTAMP,
    create_by        BIGINT         DEFAULT NULL,
    update_by        BIGINT         DEFAULT NULL,
    deleted          TINYINT        DEFAULT 0,
    remark           VARCHAR(500)   DEFAULT NULL
);

-- ========== 索引 ==========
CREATE INDEX IF NOT EXISTS idx_user_username ON sys_user(username);
CREATE INDEX IF NOT EXISTS idx_user_dept_id ON sys_user(dept_id);
CREATE INDEX IF NOT EXISTS idx_attendance_date ON att_record(attendance_date);
CREATE INDEX IF NOT EXISTS idx_attendance_user_date ON att_record(user_id, attendance_date);
CREATE INDEX IF NOT EXISTS idx_leave_user ON att_leave_request(user_id);
CREATE INDEX IF NOT EXISTS idx_approval_status ON appr_instance(status);
CREATE INDEX IF NOT EXISTS idx_notice_status_time ON sys_notice(status, publish_time);
CREATE INDEX IF NOT EXISTS idx_schedule_time ON sch_schedule(start_time, end_time);
CREATE INDEX IF NOT EXISTS idx_expense_user ON exp_request(user_id);
CREATE INDEX IF NOT EXISTS idx_appr_record_approver ON appr_record(approver_id, result);
CREATE INDEX IF NOT EXISTS idx_appr_instance_applicant ON appr_instance(applicant_id, status);
