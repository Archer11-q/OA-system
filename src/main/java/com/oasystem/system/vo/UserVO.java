package com.oasystem.system.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户信息 VO（脱敏后返回给前端）
 */
@Data
public class UserVO {

    private Long id;
    private String username;
    private String realName;
    private String employeeNo;
    private Integer gender;
    private String phone;
    private String email;
    private String avatar;
    private Long deptId;
    private String deptName;
    private LocalDate entryDate;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime lastLoginTime;

    // 角色信息
    private java.util.List<String> roleNames;
    private java.util.List<String> roles;
    private java.util.List<String> permissions;
}
