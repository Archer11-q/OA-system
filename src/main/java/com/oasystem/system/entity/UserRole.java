package com.oasystem.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户-角色关联
 */
@Data
@TableName("sys_user_role")
public class UserRole implements Serializable {

    /** 用户ID */
    private Long userId;

    /** 角色ID */
    private Long roleId;
}
