package com.oasystem.system.controller;

import com.oasystem.common.PageResult;
import com.oasystem.common.Result;
import com.oasystem.log.annotation.Log;
import com.oasystem.system.dto.UserQueryDTO;
import com.oasystem.system.entity.User;
import com.oasystem.system.service.UserService;
import com.oasystem.system.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.oasystem.system.service.UserService;
import com.oasystem.system.vo.RoleVO;
import com.oasystem.system.entity.Role;
import com.oasystem.system.dto.AssignRoleDTO;
import org.springframework.beans.BeanUtils;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理控制器
 */
@Tag(name = "用户管理", description = "用户CURD、密码重置")
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "分页查询用户列表")
    @GetMapping("/page")
    public Result<PageResult<UserVO>> page(@Parameter(description = "用户查询条件（分页+过滤）") UserQueryDTO query) {
        return Result.ok(userService.pageQuery(query));
    }

    @Operation(summary = "查询用户已分配角色")
    @GetMapping("/{id}/roles")
    public Result<List<RoleVO>> getUserRoles(@Parameter(description = "用户ID", required = true) @PathVariable("id") Long id) {
        List<Role> roles = userService.getUserRoles(id);
        List<RoleVO> vos = roles.stream().map(r -> {
            RoleVO vo = new RoleVO();
            BeanUtils.copyProperties(r, vo);
            return vo;
        }).collect(Collectors.toList());
        return Result.ok(vos);
    }

    @Operation(summary = "为用户分配角色（覆盖）")
    @PutMapping("/{id}/roles")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<Void> assignRoles(@Parameter(description = "用户ID", required = true) @PathVariable("id") Long id,
                                     @Parameter(description = "角色ID列表", required = true) @Valid @RequestBody AssignRoleDTO dto) {
        userService.assignRolesToUser(id, dto.getRoleIds());
        return Result.ok("分配成功", null);
    }

    @Operation(summary = "移除用户的若干角色")
    @DeleteMapping("/{id}/roles")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<Void> removeRoles(@Parameter(description = "用户ID", required = true) @PathVariable("id") Long id,
                                     @Parameter(description = "角色ID列表") @RequestBody AssignRoleDTO dto) {
        // 移除指定角色（实现为在现有关联中删除传入的 roleIds）
        List<Role> current = userService.getUserRoles(id);
        List<Long> remaining = current.stream().map(Role::getId).filter(rid -> !dto.getRoleIds().contains(rid)).collect(Collectors.toList());
        userService.assignRolesToUser(id, remaining);
        return Result.ok("移除成功", null);
    }

    @Operation(summary = "根据ID查询用户")
    @GetMapping("/{id}")
    public Result<UserVO> getById(@Parameter(description = "用户ID", required = true) @PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.notFound("用户不存在");
        }
        return Result.ok(userService.pageQuery(
                new UserQueryDTO()
        ).getRecords().stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElse(null));
    }

    @Log(module = "用户管理", value = "新增用户")
    @Operation(summary = "新增用户")
    @PostMapping
    public Result<Void> add(@Parameter(description = "用户信息（含用户名、密码、部门等）", required = true) @Valid @RequestBody User user) {
        userService.addUser(user);
        return Result.ok("新增成功", null);
    }

    @Log(module = "用户管理", value = "更新用户")
    @Operation(summary = "更新用户")
    @PutMapping
    public Result<Void> update(@Parameter(description = "用户信息（含ID和要更新的字段）", required = true) @Valid @RequestBody User user) {
        userService.updateUser(user);
        return Result.ok("更新成功", null);
    }

    @Log(module = "用户管理", value = "删除用户")
    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@Parameter(description = "用户ID", required = true) @PathVariable Long id) {
        userService.removeById(id);
        return Result.ok("删除成功", null);
    }

    @Log(module = "用户管理", value = "重置密码")
    @Operation(summary = "重置用户密码")
    @PutMapping("/{id}/reset-pwd")
    public Result<Void> resetPassword(@Parameter(description = "用户ID", required = true) @PathVariable Long id) {
        userService.resetPassword(id);
        return Result.ok("密码已重置为默认密码", null);
    }
}
