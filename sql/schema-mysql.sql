-- ==============================================
-- OA System - MySQL 建表脚本
-- 使用方法: mysql -u root -p oa_system < schema-mysql.sql
-- ==============================================

CREATE DATABASE IF NOT EXISTS oa_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE oa_system;

-- ---------- 系统管理模块 ----------

DROP TABLE IF EXISTS sys_role_menu;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_menu;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS sys_dept;
DROP TABLE IF EXISTS sys_notice;
DROP TABLE IF EXISTS att_leave_request;
DROP TABLE IF EXISTS att_record;
DROP TABLE IF EXISTS appr_record;
DROP TABLE IF EXISTS appr_instance;
DROP TABLE IF EXISTS appr_template;
DROP TABLE IF EXISTS sch_schedule;
DROP TABLE IF EXISTS exp_request;

-- 部门表
CREATE TABLE sys_dept (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id   BIGINT       DEFAULT 0   COMMENT '父部门ID',
    dept_name   VARCHAR(64)  NOT NULL    COMMENT '部门名称',
    leader_id   BIGINT       DEFAULT NULL COMMENT '负责人ID',
    sort        INT          DEFAULT 0   COMMENT '排序',
    status      TINYINT      DEFAULT 1   COMMENT '状态(0=停用,1=正常)',
    ancestors   VARCHAR(500) DEFAULT ''  COMMENT '祖级列表',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by   BIGINT       DEFAULT NULL,
    update_by   BIGINT       DEFAULT NULL,
    deleted     TINYINT      DEFAULT 0   COMMENT '逻辑删除',
    remark      VARCHAR(500) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 用户表
CREATE TABLE sys_user (
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
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       BIGINT       DEFAULT NULL,
    update_by       BIGINT       DEFAULT NULL,
    deleted         TINYINT      DEFAULT 0,
    remark          VARCHAR(500) DEFAULT NULL,
    INDEX idx_username (username),
    INDEX idx_dept_id (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE sys_role (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name   VARCHAR(32)  NOT NULL COMMENT '角色名称',
    role_code   VARCHAR(32)  NOT NULL UNIQUE COMMENT '角色编码',
    description VARCHAR(256) DEFAULT NULL COMMENT '角色描述',
    sort        INT          DEFAULT 0   COMMENT '排序',
    status      TINYINT      DEFAULT 1   COMMENT '状态',
    data_scope  TINYINT      DEFAULT 4   COMMENT '数据权限范围(1全部 2本部门及子部门 3本部门 4本人)',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by   BIGINT       DEFAULT NULL,
    update_by   BIGINT       DEFAULT NULL,
    deleted     TINYINT      DEFAULT 0,
    remark      VARCHAR(500) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 菜单表
CREATE TABLE sys_menu (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id   BIGINT       DEFAULT 0   COMMENT '父菜单ID',
    menu_name   VARCHAR(64)  NOT NULL    COMMENT '菜单名称',
    menu_type   TINYINT      DEFAULT 1   COMMENT '菜单类型(0=目录,1=菜单,2=按钮)',
    perms       VARCHAR(128) DEFAULT NULL COMMENT '权限标识',
    path        VARCHAR(256) DEFAULT NULL COMMENT '路由路径',
    component   VARCHAR(256) DEFAULT NULL COMMENT '前端组件',
    icon        VARCHAR(64)  DEFAULT NULL COMMENT '图标',
    sort        INT          DEFAULT 0   COMMENT '排序',
    visible     TINYINT      DEFAULT 1   COMMENT '是否显示',
    is_frame    TINYINT      DEFAULT 0   COMMENT '是否外链',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by   BIGINT       DEFAULT NULL,
    update_by   BIGINT       DEFAULT NULL,
    deleted     TINYINT      DEFAULT 0,
    remark      VARCHAR(500) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- 用户角色关联
CREATE TABLE sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联';

-- 角色菜单关联
CREATE TABLE sys_role_menu (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联';

-- ---------- 考勤 ----------
CREATE TABLE att_record (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id           BIGINT       NOT NULL,
    attendance_date   DATE         NOT NULL,
    sign_in_time      TIME         DEFAULT NULL,
    sign_out_time     TIME         DEFAULT NULL,
    status            VARCHAR(16)  DEFAULT 'ABSENT',
    sign_in_type      TINYINT      DEFAULT 1,
    sign_out_type     TINYINT      DEFAULT 1,
    work_hours        FLOAT        DEFAULT 0,
    sign_in_location  VARCHAR(256) DEFAULT NULL,
    sign_out_location VARCHAR(256) DEFAULT NULL,
    create_time       DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by         BIGINT       DEFAULT NULL,
    update_by         BIGINT       DEFAULT NULL,
    deleted           TINYINT      DEFAULT 0,
    remark            VARCHAR(500) DEFAULT NULL,
    INDEX idx_user_date (user_id, attendance_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤记录';

CREATE TABLE att_leave_request (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT        NOT NULL,
    leave_type          TINYINT       NOT NULL,
    start_date          DATE          NOT NULL,
    end_date            DATE          NOT NULL,
    days                FLOAT         NOT NULL,
    reason              VARCHAR(512)  DEFAULT NULL,
    status              TINYINT       DEFAULT 0,
    approval_comment    VARCHAR(256)  DEFAULT NULL,
    approver_id         BIGINT        DEFAULT NULL,
    approval_time       DATETIME      DEFAULT NULL,
    approval_instance_id BIGINT       DEFAULT NULL,
    create_time         DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time         DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by           BIGINT        DEFAULT NULL,
    update_by           BIGINT        DEFAULT NULL,
    deleted             TINYINT       DEFAULT 0,
    remark              VARCHAR(500)  DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请假申请';

-- ---------- 审批 ----------
CREATE TABLE appr_template (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_name   VARCHAR(64)  NOT NULL,
    template_code   VARCHAR(32)  NOT NULL UNIQUE,
    description     VARCHAR(256) DEFAULT NULL,
    approval_levels TINYINT      DEFAULT 1,
    status          TINYINT      DEFAULT 1,
    approvers_config TEXT         DEFAULT NULL COMMENT '审批人配置(JSON)',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       BIGINT       DEFAULT NULL,
    update_by       BIGINT       DEFAULT NULL,
    deleted         TINYINT      DEFAULT 0,
    remark          VARCHAR(500) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批模板';

CREATE TABLE appr_instance (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id     BIGINT        NOT NULL,
    applicant_id    BIGINT        NOT NULL,
    title           VARCHAR(128)  NOT NULL,
    content         TEXT          DEFAULT NULL,
    total_levels    TINYINT       DEFAULT 1,
    current_level   TINYINT       DEFAULT 1,
    status          TINYINT       DEFAULT 0,
    approvers_snapshot TEXT       DEFAULT NULL COMMENT '审批人快照(JSON)',
    business_type   VARCHAR(32)   DEFAULT NULL COMMENT '关联业务类型(LEAVE=请假,EXPENSE=报销)',
    business_id     BIGINT        DEFAULT NULL COMMENT '关联业务ID',
    finish_time     DATETIME      DEFAULT NULL,
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       BIGINT        DEFAULT NULL,
    update_by       BIGINT        DEFAULT NULL,
    deleted         TINYINT       DEFAULT 0,
    remark          VARCHAR(500)  DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批实例';

CREATE TABLE appr_record (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    instance_id   BIGINT       NOT NULL,
    level         TINYINT      NOT NULL,
    approver_id   BIGINT       NOT NULL,
    result        TINYINT      DEFAULT 0,
    comment       VARCHAR(256) DEFAULT NULL,
    approval_time DATETIME     DEFAULT NULL,
    create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by     BIGINT       DEFAULT NULL,
    update_by     BIGINT       DEFAULT NULL,
    deleted       TINYINT      DEFAULT 0,
    remark        VARCHAR(500) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批记录';

-- ---------- 公告 ----------
CREATE TABLE sys_notice (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    title        VARCHAR(128)  NOT NULL,
    content      TEXT          DEFAULT NULL,
    notice_type  TINYINT       DEFAULT 1,
    status       TINYINT       DEFAULT 0,
    is_top       TINYINT       DEFAULT 0,
    publish_time DATETIME      DEFAULT NULL,
    publisher_id BIGINT        DEFAULT NULL,
    view_count   INT           DEFAULT 0,
    create_time  DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by    BIGINT        DEFAULT NULL,
    update_by    BIGINT        DEFAULT NULL,
    deleted      TINYINT       DEFAULT 0,
    remark       VARCHAR(500)  DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告通知';

-- ---------- 日程 ----------
CREATE TABLE sch_schedule (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    title          VARCHAR(128)  NOT NULL,
    content        VARCHAR(1024) DEFAULT NULL,
    start_time     DATETIME      NOT NULL,
    end_time       DATETIME      NOT NULL,
    schedule_type  TINYINT       DEFAULT 1,
    priority       TINYINT       DEFAULT 1,
    location       VARCHAR(256)  DEFAULT NULL,
    creator_id     BIGINT        NOT NULL,
    participant_ids TEXT         DEFAULT NULL,
    status           TINYINT       DEFAULT 0,
    visibility       TINYINT       DEFAULT 1   COMMENT '可见性(1私有 2部门 3公开)',
    reminder_minutes INT           DEFAULT NULL COMMENT '提前提醒分钟数(NULL=不提醒)',
    reminder_sent    TINYINT       DEFAULT 0   COMMENT '提醒是否已发送(0未发送 1已发送)',
    create_time    DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time    DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by      BIGINT        DEFAULT NULL,
    update_by      BIGINT        DEFAULT NULL,
    deleted        TINYINT       DEFAULT 0,
    remark         VARCHAR(500)  DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日程管理';

-- ---------- 系统日志 ----------
CREATE TABLE sys_oper_log (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id        BIGINT       DEFAULT NULL COMMENT '操作人ID',
    username       VARCHAR(32)  DEFAULT NULL COMMENT '操作人用户名',
    module         VARCHAR(64)  DEFAULT NULL COMMENT '操作模块',
    operation      VARCHAR(128) DEFAULT NULL COMMENT '操作描述',
    method         VARCHAR(256) DEFAULT NULL COMMENT '请求方法',
    request_method VARCHAR(16)  DEFAULT NULL COMMENT '请求方式',
    url            VARCHAR(256) DEFAULT NULL COMMENT '请求URL',
    ip             VARCHAR(64)  DEFAULT NULL COMMENT '操作IP',
    request_params TEXT         DEFAULT NULL COMMENT '请求参数',
    result         TEXT         DEFAULT NULL COMMENT '返回结果',
    cost_time      BIGINT       DEFAULT 0   COMMENT '耗时(毫秒)',
    status         TINYINT      DEFAULT 1   COMMENT '状态(0失败,1成功)',
    error_msg      VARCHAR(1024) DEFAULT NULL COMMENT '错误信息',
    create_time    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_oper_log_user (user_id),
    INDEX idx_oper_log_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志';

-- ---------- 报销 ----------
CREATE TABLE exp_request (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT         NOT NULL,
    title               VARCHAR(128)   NOT NULL,
    expense_type        TINYINT        NOT NULL,
    amount              DECIMAL(12,2)  NOT NULL,
    description         VARCHAR(512)   DEFAULT NULL,
    attachments         TEXT           DEFAULT NULL,
    status              TINYINT        DEFAULT 0,
    approver_id         BIGINT         DEFAULT NULL,
    approval_comment    VARCHAR(256)   DEFAULT NULL,
    approval_time       DATETIME       DEFAULT NULL,
    approval_instance_id BIGINT        DEFAULT NULL,
    create_time         DATETIME       DEFAULT CURRENT_TIMESTAMP,
    update_time         DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by           BIGINT         DEFAULT NULL,
    update_by           BIGINT         DEFAULT NULL,
    deleted             TINYINT        DEFAULT 0,
    remark              VARCHAR(500)   DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报销申请';

-- ========== 索引 ==========
CREATE INDEX idx_appr_record_approver ON appr_record(approver_id, result);
CREATE INDEX idx_appr_instance_applicant ON appr_instance(applicant_id, status);
