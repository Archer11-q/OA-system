package com.oasystem.common.constant;

/**
 * 系统常量
 * <p>
 * 定义审批状态、考勤类型等业务枚举常量。
 * 后续开发中根据模块需求扩展。
 */
public final class Constants {

    private Constants() {
        // 工具类禁止实例化
    }

    // ========== 通用状态 ==========
    /** 正常/启用 */
    public static final int STATUS_ENABLE = 1;
    /** 禁用/停用 */
    public static final int STATUS_DISABLE = 0;

    // ========== 审批状态 ==========
    /** 审批中 */
    public static final int APPROVAL_PENDING = 0;
    /** 审批通过 */
    public static final int APPROVAL_APPROVED = 1;
    /** 审批驳回 */
    public static final int APPROVAL_REJECTED = 2;
    /** 已撤回 */
    public static final int APPROVAL_CANCELLED = 3;

    // ========== 考勤状态 ==========
    /** 正常 */
    public static final String ATTENDANCE_NORMAL = "NORMAL";
    /** 迟到 */
    public static final String ATTENDANCE_LATE = "LATE";
    /** 早退 */
    public static final String ATTENDANCE_EARLY = "EARLY";
    /** 缺勤 */
    public static final String ATTENDANCE_ABSENT = "ABSENT";

    // ========== 考勤时间配置 ==========
    /** 上班时间（小时） */
    public static final int WORK_START_HOUR = 9;
    /** 下班时间（小时） */
    public static final int WORK_END_HOUR = 18;

    // ========== 业务类型（审批集成） ==========
    /** 请假审批 */
    public static final String BUSINESS_TYPE_LEAVE = "LEAVE";
    /** 报销审批 */
    public static final String BUSINESS_TYPE_EXPENSE = "EXPENSE";

    // ========== 请假类型 ==========
    /** 年假 */
    public static final int LEAVE_ANNUAL = 1;
    /** 事假 */
    public static final int LEAVE_PERSONAL = 2;
    /** 病假 */
    public static final int LEAVE_SICK = 3;
    /** 调休 */
    public static final int LEAVE_COMPENSATORY = 4;

    // ========== 菜单类型 ==========
    /** 目录 */
    public static final int MENU_TYPE_DIR = 0;
    /** 菜单 */
    public static final int MENU_TYPE_MENU = 1;
    /** 按钮/权限 */
    public static final int MENU_TYPE_BUTTON = 2;
}
