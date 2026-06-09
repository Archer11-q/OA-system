package com.oasystem.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oasystem.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 系统用户实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class User extends BaseEntity {

    /** 用户名（登录账号） */
    private String username;

    /** 密码（BCrypt加密） */
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 工号 */
    private String employeeNo;

    /** 性别（0=女，1=男） */
    private Integer gender;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 头像URL */
    private String avatar;

    /** 所属部门ID */
    private Long deptId;

    /** 入职日期 */
    private LocalDate entryDate;

    /** 用户状态（0=禁用，1=启用） */
    private Integer status;

    /** 最后登录时间 */
    private java.time.LocalDateTime lastLoginTime;

    /** 最后登录IP */
    private String lastLoginIp;
}
