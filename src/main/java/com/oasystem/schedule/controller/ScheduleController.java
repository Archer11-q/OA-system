package com.oasystem.schedule.controller;

import com.oasystem.common.Result;
import com.oasystem.log.annotation.Log;
import com.oasystem.schedule.entity.Schedule;
import com.oasystem.schedule.service.ScheduleService;
import com.oasystem.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 日程管理 Controller
 */
@Tag(name = "日程管理", description = "日程的增删改查与日期范围查询")
@RestController
@RequestMapping("/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final SecurityUtils securityUtils;

    @Operation(summary = "日程列表（按日期范围）")
    @GetMapping("/list")
    public Result<List<Schedule>> list(
            @Parameter(description = "开始日期（yyyy-MM-dd）") @RequestParam(value = "startDate", required = false) String startDate,
            @Parameter(description = "结束日期（yyyy-MM-dd）") @RequestParam(value = "endDate", required = false) String endDate) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) return Result.unauthorized("请先登录");

        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        if (startDate != null && !startDate.isEmpty()) {
            startTime = LocalDate.parse(startDate).atStartOfDay();
        }
        if (endDate != null && !endDate.isEmpty()) {
            endTime = LocalDate.parse(endDate).atTime(LocalTime.MAX);
        }

        List<Schedule> list = scheduleService.listByDateRange(userId, startTime, endTime);
        return Result.ok(list);
    }

    @Operation(summary = "日程详情")
    @GetMapping("/{id}")
    public Result<Schedule> getById(@Parameter(description = "日程ID", required = true) @PathVariable Long id) {
        Schedule s = scheduleService.getById(id);
        if (s == null) return Result.notFound("日程不存在");
        return Result.ok(s);
    }

    @Log(module = "日程管理", value = "新增日程")
    @Operation(summary = "新增日程")
    @PostMapping
    public Result<Void> create(@Parameter(description = "日程信息（标题+时间+类型+参与人）", required = true) @Valid @RequestBody Schedule schedule) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) return Result.unauthorized("请先登录");
        schedule.setCreatorId(userId);
        scheduleService.createSchedule(schedule);
        return Result.ok("日程创建成功", null);
    }

    @Operation(summary = "更新日程")
    @PutMapping
    public Result<Void> update(@Parameter(description = "日程信息（含ID和要更新的字段）", required = true) @Valid @RequestBody Schedule schedule) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) return Result.unauthorized("请先登录");
        // 确保只能更新自己的日程
        Schedule exist = scheduleService.getById(schedule.getId());
        if (exist == null) return Result.notFound("日程不存在");
        if (!exist.getCreatorId().equals(userId)) {
            return Result.forbidden("只能修改自己创建的日程");
        }
        scheduleService.updateSchedule(schedule);
        return Result.ok("日程更新成功", null);
    }

    @Operation(summary = "删除日程")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@Parameter(description = "日程ID", required = true) @PathVariable Long id) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) return Result.unauthorized("请先登录");
        scheduleService.deleteSchedule(id, userId);
        return Result.ok("日程删除成功", null);
    }

    @Operation(summary = "获取即将到来的日程提醒（未来24小时内）")
    @GetMapping("/reminders")
    public Result<List<Schedule>> reminders() {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) return Result.unauthorized("请先登录");
        return Result.ok(scheduleService.getUpcomingReminders(userId));
    }
}
