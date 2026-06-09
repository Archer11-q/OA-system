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

    /** 状态 */
    private Integer status;
}
