package com.oasystem.system.controller;

import com.oasystem.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.oasystem.system.service.DeptService;
import com.oasystem.system.entity.Dept;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

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

    private final DeptService deptService;

    @Operation(summary = "查询部门树")
    @GetMapping("/tree")
    public Result<List<Dept>> tree() {
        List<Dept> list = deptService.listAll();
        // 组装树
        Map<Long, Dept> map = list.stream().collect(java.util.stream.Collectors.toMap(Dept::getId, d -> d));
        List<Dept> roots = new java.util.ArrayList<>();
        for (Dept d : list) {
            if (d.getParentId() == null || d.getParentId() == 0L) {
                roots.add(d);
            } else {
                Dept p = map.get(d.getParentId());
                if (p != null) {
                    // 没有 children 字段，前端可以按 ancestors 组装，直接返回扁平列表或根节点列表
                }
            }
        }
        return Result.ok(roots);
    }

    @Operation(summary = "新增部门")
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<Void> add(@Valid @RequestBody Dept dept) {
        deptService.createDept(dept);
        return Result.ok("新增成功", null);
    }

    @Operation(summary = "更新部门")
    @PutMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<Void> update(@Valid @RequestBody Dept dept) {
        deptService.updateDept(dept);
        return Result.ok("更新成功", null);
    }

    @Operation(summary = "删除部门")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        deptService.deleteDept(id);
        return Result.ok("删除成功", null);
    }
}
