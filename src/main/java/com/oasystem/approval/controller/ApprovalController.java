package com.oasystem.approval.controller;

import com.oasystem.approval.dto.ApproveDTO;
import com.oasystem.approval.dto.StartApprovalDTO;
import com.oasystem.approval.entity.ApprovalInstance;
import com.oasystem.approval.entity.ApprovalRecord;
import com.oasystem.approval.service.ApprovalService;
import com.oasystem.common.Result;
import com.oasystem.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 审批中心 Controller — 发起审批/待审批/已审批/我的申请/审批操作
 */
@Tag(name = "审批中心", description = "发起审批/待审批/已审批/我的申请/审批操作")
@RestController
@RequestMapping("/approval")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;
    private final SecurityUtils securityUtils;

    @Operation(summary = "发起审批")
    @PostMapping("/start")
    public Result<Long> start(@Valid @RequestBody StartApprovalDTO dto) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) return Result.unauthorized("请先登录");
        Long id = approvalService.start(dto, userId);
        return Result.ok(id);
    }

    @Operation(summary = "待审批列表")
    @GetMapping("/todo")
    public Result<List<ApprovalInstance>> todo() {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) return Result.unauthorized("请先登录");
        return Result.ok(approvalService.getTodo(userId));
    }

    @Operation(summary = "已审批列表")
    @GetMapping("/done")
    public Result<List<ApprovalInstance>> done() {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) return Result.unauthorized("请先登录");
        return Result.ok(approvalService.getDone(userId));
    }

    @Operation(summary = "我的申请")
    @GetMapping("/my")
    public Result<List<ApprovalInstance>> my() {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) return Result.unauthorized("请先登录");
        return Result.ok(approvalService.getMy(userId));
    }

    @Operation(summary = "审批操作（同意/驳回）")
    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable("id") Long id, @Valid @RequestBody ApproveDTO dto) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) return Result.unauthorized("请先登录");
        approvalService.approve(id, dto.getResult(), dto.getComment(), userId);
        return Result.ok("审批完成", null);
    }

    @Operation(summary = "审批记录")
    @GetMapping("/{id}/records")
    public Result<List<ApprovalRecord>> records(@PathVariable("id") Long id) {
        return Result.ok(approvalService.getRecords(id));
    }
}
