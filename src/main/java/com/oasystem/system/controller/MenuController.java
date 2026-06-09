package com.oasystem.system.controller;

import com.oasystem.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    @Operation(summary = "查询菜单树")
    @GetMapping("/tree")
    public Result<String> tree() {
        // TODO: 实现菜单树查询
        return Result.ok("菜单树 - 待实现");
    }

    @Operation(summary = "新增菜单")
    @PostMapping
    public Result<String> add() {
        // TODO: 实现新增菜单
        return Result.ok("新增菜单 - 待实现");
    }
}
