package com.oasystem.system.controller;

import com.oasystem.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;

import com.oasystem.system.dto.RoleDTO;
import com.oasystem.system.entity.Role;
import com.oasystem.system.service.RoleService;
import com.oasystem.system.vo.RoleVO;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色管理控制器
 * <p>
 * TODO: 实现角色的CURD、分配权限、分配用户
 */
@Tag(name = "角色管理", description = "角色CURD、权限分配")
@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;

    @Operation(summary = "查询角色列表")
    @GetMapping("/list")
    public Result<List<RoleVO>> list() {
        List<Role> roles = roleService.listAll();
        List<RoleVO> vos = roles.stream().map(r -> {
            RoleVO vo = new RoleVO();
            BeanUtils.copyProperties(r, vo);
            return vo;
        }).collect(Collectors.toList());
        return Result.ok(vos);
    }

    @Operation(summary = "根据ID获取角色详情")
    @GetMapping("/{id}")
    public Result<RoleVO> getById(@Parameter(description = "角色ID", required = true) @PathVariable Long id) {
        Role r = roleService.getByIdWithCheck(id);
        if (r == null) return Result.notFound("角色不存在");
        RoleVO vo = new RoleVO();
        BeanUtils.copyProperties(r, vo);
        return Result.ok(vo);
    }

    @Operation(summary = "新增角色")
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<Void> add(@Parameter(description = "角色信息（名称+编码+数据范围）", required = true) @Valid @RequestBody RoleDTO dto) {
        Role role = new Role();
        BeanUtils.copyProperties(dto, role);
        roleService.createRole(role);
        return Result.ok("新增成功", null);
    }

    @Operation(summary = "更新角色")
    @PutMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<Void> update(@Parameter(description = "角色信息（含ID和要更新的字段）", required = true) @Valid @RequestBody RoleDTO dto) {
        Role role = new Role();
        BeanUtils.copyProperties(dto, role);
        roleService.updateRole(role);
        return Result.ok("更新成功", null);
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<Void> delete(@Parameter(description = "角色ID", required = true) @PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.ok("删除成功", null);
    }

    @Operation(summary = "为角色分配菜单权限（覆盖）")
    @PutMapping("/{id}/menus")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<Void> assignMenus(@Parameter(description = "角色ID", required = true) @PathVariable("id") Long id,
                                     @Parameter(description = "菜单ID列表 {\"menuIds\": [1,2,3]}") @RequestBody java.util.Map<String, java.util.List<Long>> body) {
        java.util.List<Long> menuIds = body.getOrDefault("menuIds", java.util.Collections.emptyList());
        roleService.assignMenus(id, menuIds);
        return Result.ok("分配成功", null);
    }
}
