package com.oasystem.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oasystem.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单/权限实体
 * <p>
 * 使用"菜单-按钮"的层级结构管理权限：
 * - 目录（type=0）：顶级分组
 * - 菜单（type=1）：左侧菜单项，对应前端路由
 * - 按钮（type=2）：页面内的操作按钮权限
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class Menu extends BaseEntity {

    /** 父菜单ID（0=顶级） */
    private Long parentId;

    /** 菜单名称 */
    private String menuName;

    /** 菜单类型（0=目录，1=菜单，2=按钮） */
    private Integer menuType;

    /** 权限标识（如 system:user:add） */
    private String perms;

    /** 前端路由路径 */
    private String path;

    /** 前端组件路径 */
    private String component;

    /** 菜单图标 */
    private String icon;

    /** 排序 */
    private Integer sort;

    /** 状态（0=隐藏，1=显示） */
    private Integer visible;

    /** 是否外链（0=否，1=是） */
    private Integer isFrame;
}
