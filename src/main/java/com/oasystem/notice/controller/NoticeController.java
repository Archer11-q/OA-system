package com.oasystem.notice.controller;

import com.oasystem.common.Result;
import com.oasystem.notice.entity.Notice;
import com.oasystem.notice.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Tag(name = "公告通知", description = "公告发布/列表/详情/删除")
@RestController
@RequestMapping("/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(summary = "公告列表")
    @GetMapping("/page")
    public Result<List<Notice>> list() {
        return Result.ok(noticeService.listAll());
    }

    @Operation(summary = "公告详情")
    @GetMapping("/{id}")
    public Result<Notice> getById(@PathVariable Long id) {
        Notice n = noticeService.getById(id);
        if (n == null) return Result.notFound("公告不存在");
        return Result.ok(n);
    }

    @Operation(summary = "发布公告（管理员）")
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<Void> create(@Valid @RequestBody Notice notice) {
        noticeService.createNotice(notice);
        return Result.ok("发布成功", null);
    }

    @Operation(summary = "编辑公告（管理员）")
    @PutMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<Void> update(@Valid @RequestBody Notice notice) {
        noticeService.updateNotice(notice);
        return Result.ok("更新成功", null);
    }

    @Operation(summary = "删除公告（管理员）")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return Result.ok("删除成功", null);
    }
}

