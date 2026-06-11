package com.oasystem.approval.controller;

import com.oasystem.approval.entity.ApprovalTemplate;
import com.oasystem.approval.service.ApprovalTemplateService;
import com.oasystem.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 审批模板管理 Controller
 */
@Tag(name = "审批模板管理", description = "审批模板的增删改查")
@RestController
@RequestMapping("/approval/template")
@RequiredArgsConstructor
public class ApprovalTemplateController {

    private final ApprovalTemplateService templateService;

    @Operation(summary = "模板列表")
    @GetMapping("/list")
    public Result<List<ApprovalTemplate>> list() {
        return Result.ok(templateService.listAll());
    }

    @Operation(summary = "模板详情")
    @GetMapping("/{id}")
    public Result<ApprovalTemplate> getById(@Parameter(description = "模板ID", required = true) @PathVariable Long id) {
        ApprovalTemplate t = templateService.getById(id);
        return Result.ok(t);
    }

    @Operation(summary = "新增模板（管理员）")
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<Void> create(@Parameter(description = "模板信息（名称+编码+审批人配置）", required = true) @Valid @RequestBody ApprovalTemplate template) {
        templateService.create(template);
        return Result.ok("模板创建成功", null);
    }

    @Operation(summary = "更新模板（管理员）")
    @PutMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<Void> update(@Parameter(description = "模板信息（含ID和要更新的字段）", required = true) @Valid @RequestBody ApprovalTemplate template) {
        templateService.update(template);
        return Result.ok("模板更新成功", null);
    }

    @Operation(summary = "删除模板（管理员）")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<Void> delete(@Parameter(description = "模板ID", required = true) @PathVariable Long id) {
        templateService.delete(id);
        return Result.ok("模板删除成功", null);
    }
}
