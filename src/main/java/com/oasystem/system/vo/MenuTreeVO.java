package com.oasystem.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 菜单树 VO
 */
@Data
@Schema(description = "菜单树节点视图对象")
public class MenuTreeVO {

    @Schema(description = "菜单ID")
    private Long id;

    @Schema(description = "父菜单ID")
    private Long parentId;

    @Schema(description = "菜单名称")
    private String menuName;

    @Schema(description = "路由路径")
    private String path;

    @Schema(description = "前端组件路径")
    private String component;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "菜单类型（1=目录，2=菜单，3=按钮）")
    private Integer menuType;

    @Schema(description = "排序号")
    private Integer sort;

    @Schema(description = "是否可见（0=隐藏，1=显示）")
    private Integer visible;

    @Schema(description = "子菜单列表")
    private List<MenuTreeVO> children;
}
