package com.oasystem.system.controller;

import com.oasystem.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 部门管理控制器
 * <p>
 * TODO: 实现部门树查询、CURD
 */
@Tag(name = "部门管理", description = "部门树、CURD")
@RestController
@RequestMapping("/system/dept")
@RequiredArgsConstructor
public class DeptController {

    @Operation(summary = "查询部门树")
    @GetMapping("/tree")
    public Result<String> tree() {
        // TODO: 实现部门树查询
        return Result.ok("部门树 - 待实现");
    }

    @Operation(summary = "新增部门")
    @PostMapping
    public Result<String> add() {
        // TODO: 实现新增部门
        return Result.ok("新增部门 - 待实现");
    }
}
