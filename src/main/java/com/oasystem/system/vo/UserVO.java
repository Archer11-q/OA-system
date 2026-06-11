package com.oasystem.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户信息 VO（脱敏后返回给前端）
 */
@Data
@Schema(description = "用户信息视图对象")
public class UserVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "工号")
    private String employeeNo;

    @Schema(description = "性别（0=女，1=男）")
    private Integer gender;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "所属部门ID")
    private Long deptId;

    @Schema(description = "所属部门名称")
    private String deptName;

    @Schema(description = "入职日期")
    private LocalDate entryDate;

    @Schema(description = "状态（0=禁用，1=启用）")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;

    @Schema(description = "角色名称列表")
    private java.util.List<String> roleNames;

    @Schema(description = "角色编码列表")
    private java.util.List<String> roles;

    @Schema(description = "权限标识列表")
    private java.util.List<String> permissions;
}
