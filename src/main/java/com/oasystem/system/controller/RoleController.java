package com.oasystem.system.controller;

import com.oasystem.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    @Operation(summary = "查询角色列表")
    @GetMapping("/list")
    public Result<String> list() {
        // TODO: 实现角色列表查询
        return Result.ok("角色列表 - 待实现");
    }

    @Operation(summary = "新增角色")
    @PostMapping
    public Result<String> add() {
        // TODO: 实现新增角色
        return Result.ok("新增角色 - 待实现");
    }
}
