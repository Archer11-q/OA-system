package com.oasystem.system.controller;

import com.oasystem.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.oasystem.system.service.MenuService;
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
    private final MenuService menuService;
    private final SecurityUtils securityUtils;

    @Operation(summary = "查询菜单树")
    @GetMapping("/tree")
    public Result<List<MenuTreeVO>> tree(@Parameter(description = "是否查询全部菜单（管理员用），默认false仅查用户权限内的菜单")
                                         @RequestParam(value = "all", required = false, defaultValue = "false") boolean all) {
        Long userId = securityUtils.getCurrentUserId();
        List<MenuTreeVO> roots = menuService.tree(all, userId);
        return Result.ok(roots);
    }

    @Operation(summary = "新增菜单")
    @PostMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('system:menu:add')")
    public Result<Void> add(@Parameter(description = "菜单信息（含名称、路径、权限标识等）", required = true) @RequestBody Menu menu) {
        if (menu == null) return Result.badRequest("参数为空");
        try {
            menuService.add(menu);
            return Result.ok("新增成功", null);
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        } catch (IllegalStateException e) {
            return Result.badRequest(e.getMessage());
        }
    }

    @Operation(summary = "删除菜单")
    @DeleteMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('system:menu:delete')")
    public Result<Void> delete(@Parameter(description = "菜单ID", required = true) @PathVariable Long id) {
        if (id == null) return Result.badRequest("参数为空");
        try {
            menuService.delete(id);
            return Result.ok("删除成功", null);
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        } catch (IllegalStateException e) {
            return Result.badRequest(e.getMessage());
        }
    }

    @Operation(summary = "更新菜单")
    @PutMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('system:menu:edit')")
    public Result<Void> update(@Parameter(description = "菜单信息（含ID和要更新的字段）", required = true) @RequestBody Menu menu) {
        if (menu == null || menu.getId() == null) return Result.badRequest("参数错误");
        try {
            menuService.update(menu);
            return Result.ok("更新成功", null);
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        } catch (IllegalStateException e) {
            return Result.badRequest(e.getMessage());
        }
    }
}
