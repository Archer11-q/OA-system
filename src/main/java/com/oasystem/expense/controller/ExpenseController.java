package com.oasystem.expense.controller;

import com.oasystem.common.Result;
import com.oasystem.expense.entity.ExpenseRequest;
import com.oasystem.expense.service.ExpenseService;
import com.oasystem.log.annotation.Log;
import com.oasystem.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 报销管理 Controller
 */
@Tag(name = "报销管理", description = "报销申请的提交/查询/修改/删除与统计")
@RestController
@RequestMapping("/expense")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;
    private final SecurityUtils securityUtils;

    @Operation(summary = "我的报销列表")
    @GetMapping("/list")
    public Result<List<ExpenseRequest>> list(
            @RequestParam(value = "status", required = false) Integer status) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) return Result.unauthorized("请先登录");
        return Result.ok(expenseService.listByUser(userId, status));
    }

    @Operation(summary = "报销详情")
    @GetMapping("/{id}")
    public Result<ExpenseRequest> getById(@PathVariable Long id) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) return Result.unauthorized("请先登录");
        ExpenseRequest e = expenseService.getById(id);
        if (e == null) return Result.notFound("报销申请不存在");
        // 只能查看自己的报销
        if (!e.getUserId().equals(userId)) {
            return Result.forbidden("只能查看自己的报销申请");
        }
        return Result.ok(e);
    }

    @Log(module = "报销管理", value = "提交报销")
    @Operation(summary = "提交报销申请")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody ExpenseRequest expense) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) return Result.unauthorized("请先登录");
        expense.setUserId(userId);
        expenseService.create(expense);
        return Result.ok("报销申请提交成功", null);
    }

    @Operation(summary = "修改报销申请")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody ExpenseRequest expense) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) return Result.unauthorized("请先登录");
        expenseService.update(expense, userId);
        return Result.ok("报销申请更新成功", null);
    }

    @Operation(summary = "删除报销申请")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) return Result.unauthorized("请先登录");
        expenseService.delete(id, userId);
        return Result.ok("报销申请删除成功", null);
    }

    @Operation(summary = "报销统计")
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) return Result.unauthorized("请先登录");
        return Result.ok(expenseService.stats(userId));
    }
}
