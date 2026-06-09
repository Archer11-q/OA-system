package com.oasystem.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oasystem.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class Role extends BaseEntity {

    /** 角色名称 */
    private String roleName;

    /** 角色编码（如 ROLE_ADMIN, ROLE_MANAGER） */
    private String roleCode;

    /** 角色描述 */
    private String description;

    /** 角色排序 */
    private Integer sort;

    /** 状态（0=禁用，1=启用） */
    private Integer status;

    /** 数据权限范围（1=全部，2=本部门及子部门，3=本部门，4=仅本人） */
    private Integer dataScope;
}
