package com.oasystem.system.vo;

import lombok.Data;

import java.util.List;

/**
 * 菜单树 VO
 */
@Data
public class MenuTreeVO {

    private Long id;
    private Long parentId;
    private String menuName;
    private String path;
    private String component;
    private String icon;
    private Integer menuType;
    private Integer sort;
    private Integer visible;

    /** 子菜单列表 */
    private List<MenuTreeVO> children;
}
