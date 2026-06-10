package com.oasystem.system.controller;

import com.oasystem.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.oasystem.system.mapper.MenuMapper;
import com.oasystem.system.vo.MenuTreeVO;
import com.oasystem.system.entity.Menu;
import com.oasystem.security.SecurityUtils;
import com.oasystem.system.mapper.RoleMenuMapper;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 菜单管理控制器
 * <p>
 * TODO: 实现菜单树查询、CURD
 */
@Tag(name = "菜单管理", description = "菜单树、权限管理")
@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
public class MenuController {
    private final MenuMapper menuMapper;
    private final SecurityUtils securityUtils;
    private final RoleMenuMapper roleMenuMapper;

    @Operation(summary = "查询菜单树")
    @GetMapping("/tree")
    public Result<List<MenuTreeVO>> tree(@RequestParam(value = "all", required = false, defaultValue = "false") boolean all) {
        Long userId = securityUtils.getCurrentUserId();
        List<Menu> menus;
        if (all) {
            menus = menuMapper.selectList(null);
        } else {
            menus = menuMapper.selectMenusByUserId(userId);
        }
        // 转换为 VO 并组装树
        List<MenuTreeVO> vos = menus.stream().map(m -> {
            MenuTreeVO vo = new MenuTreeVO();
            vo.setId(m.getId());
            vo.setParentId(m.getParentId());
            vo.setMenuName(m.getMenuName());
            vo.setPath(m.getPath());
            vo.setComponent(m.getComponent());
            vo.setIcon(m.getIcon());
            vo.setMenuType(m.getMenuType());
            vo.setSort(m.getSort());
            vo.setVisible(m.getVisible());
            return vo;
        }).collect(Collectors.toList());

        Map<Long, MenuTreeVO> map = vos.stream().collect(Collectors.toMap(MenuTreeVO::getId, v -> v));
        List<MenuTreeVO> roots = vos.stream().filter(v -> v.getParentId() == null || v.getParentId() == 0L).collect(Collectors.toList());
        for (MenuTreeVO vo : vos) {
            if (vo.getParentId() != null && vo.getParentId() != 0L) {
                MenuTreeVO parent = map.get(vo.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) parent.setChildren(new java.util.ArrayList<>());
                    parent.getChildren().add(vo);
                }
            }
        }
        return Result.ok(roots);
    }

    @Operation(summary = "新增菜单")
    @PostMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<Void> add(@RequestBody Menu menu) {
        if (menu == null) return Result.badRequest("参数为空");
        menuMapper.insert(menu);
        return Result.ok("新增成功", null);
    }

    @Operation(summary = "删除菜单")
    @DeleteMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        if (id == null) return Result.badRequest("参数为空");
        // 删除菜单
        menuMapper.deleteById(id);
        // 清理 role-menu 关联
        roleMenuMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.oasystem.system.entity.RoleMenu>().eq(com.oasystem.system.entity.RoleMenu::getMenuId, id));
        return Result.ok("删除成功", null);
    }
}
