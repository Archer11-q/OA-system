package com.oasystem.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oasystem.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dept")
public class Dept extends BaseEntity {

    /** 父部门ID（0=顶级部门） */
    private Long parentId;

    /** 部门名称 */
    private String deptName;

    /** 部门负责人ID */
    private Long leaderId;

    /** 部门排序 */
    private Integer sort;

    /** 状态（0=禁用，1=启用） */
    private Integer status;

    /** 所有父级ID（用逗号分隔，如 "0,1,5"） */
    private String ancestors;
}
