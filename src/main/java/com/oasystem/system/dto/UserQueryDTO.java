package com.oasystem.system.dto;

import lombok.Data;

/**
 * 用户查询DTO
 */
@Data
public class UserQueryDTO {

    /** 当前页码 */
    private Integer pageNum = 1;

    /** 每页条数 */
    private Integer pageSize = 10;

    /** 用户名（模糊查询） */
    private String username;

    /** 真实姓名（模糊查询） */
    private String realName;

    /** 手机号 */
    private String phone;

    /** 所属部门ID */
    private Long deptId;

    /** 允许查询的部门ID集合（由 dataScope 决定） */
    private java.util.List<Long> allowedDeptIds;

    /** 仅按用户名精确过滤（用于 dataScope=4） */
    private String onlyUsername;

    /** 状态 */
    private Integer status;
}
