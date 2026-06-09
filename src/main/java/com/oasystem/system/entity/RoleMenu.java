package com.oasystem.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 角色-菜单关联
 */
@Data
@TableName("sys_role_menu")
public class RoleMenu implements Serializable {

    /** 角色ID */
    private Long roleId;

    /** 菜单ID */
    private Long menuId;
}
