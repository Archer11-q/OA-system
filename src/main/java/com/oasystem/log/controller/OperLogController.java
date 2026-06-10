package com.oasystem.log.controller;

import com.oasystem.common.PageResult;
import com.oasystem.common.Result;
import com.oasystem.log.entity.OperLog;
import com.oasystem.log.service.OperLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志 Controller（管理员可查看）
 */
@Tag(name = "操作日志", description = "操作日志查询与清理")
@RestController
@RequestMapping("/log")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class OperLogController {

    private final OperLogService operLogService;

    @Operation(summary = "操作日志列表（分页）")
    @GetMapping("/page")
    public Result<PageResult<OperLog>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) Integer status) {
        return Result.ok(operLogService.page(pageNum, pageSize, username, module, status));
    }

    @Operation(summary = "日志详情")
    @GetMapping("/{id}")
    public Result<OperLog> getById(@PathVariable Long id) {
        return Result.ok(operLogService.getById(id));
    }

    @Operation(summary = "清理旧日志（管理员）")
    @DeleteMapping("/clean")
    public Result<Integer> clean(@RequestParam(defaultValue = "90") int days) {
        int count = operLogService.cleanOldLogs(days);
        return Result.ok("清理了 " + count + " 条日志", count);
    }
}
