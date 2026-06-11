package com.oasystem.attendance.controller;

import com.oasystem.attendance.dto.LeaveRequestDTO;
import com.oasystem.attendance.dto.SignInDTO;
import com.oasystem.attendance.entity.Attendance;
import com.oasystem.attendance.entity.LeaveRequest;
import com.oasystem.attendance.service.AttendanceService;
import com.oasystem.common.Result;
import com.oasystem.log.annotation.Log;
import com.oasystem.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 考勤管理 Controller
 */
@Tag(name = "考勤管理", description = "签到/签退/请假/考勤统计")
@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final SecurityUtils securityUtils;

    @Log(module = "考勤管理", value = "签到")
    @Operation(summary = "签到")
    @PostMapping("/sign-in")
    public Result<Void> signIn(@Parameter(description = "签到信息（含位置）", required = true) @Valid @RequestBody SignInDTO dto) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) return Result.unauthorized("请先登录");
        attendanceService.signIn(userId, dto.getLocation());
        return Result.ok("签到成功", null);
    }

    @Log(module = "考勤管理", value = "签退")
    @Operation(summary = "签退")
    @PostMapping("/sign-out")
    public Result<Void> signOut(@Parameter(description = "签退信息（含位置）", required = true) @Valid @RequestBody SignInDTO dto) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) return Result.unauthorized("请先登录");
        attendanceService.signOut(userId, dto.getLocation());
        return Result.ok("签退成功", null);
    }

    @Operation(summary = "考勤记录（按月）")
    @GetMapping("/records")
    public Result<List<Attendance>> records(
            @Parameter(description = "月份（yyyy-MM格式），默认当前月") @RequestParam(value = "month", required = false) String month) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) return Result.unauthorized("请先登录");
        if (month == null || month.isEmpty()) {
            java.time.LocalDate now = java.time.LocalDate.now();
            month = String.format("%04d-%02d", now.getYear(), now.getMonthValue());
        }
        return Result.ok(attendanceService.getRecords(userId, month));
    }

    @Log(module = "考勤管理", value = "申请请假")
    @Operation(summary = "申请请假")
    @PostMapping("/leave")
    public Result<Long> applyLeave(@Parameter(description = "请假信息（类型+起止日期+天数）", required = true) @Valid @RequestBody LeaveRequestDTO dto) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) return Result.unauthorized("请先登录");
        LeaveRequest req = new LeaveRequest();
        req.setUserId(userId);
        req.setLeaveType(dto.getLeaveType());
        req.setStartDate(dto.getStartDate());
        req.setEndDate(dto.getEndDate());
        req.setDays(dto.getDays());
        req.setReason(dto.getReason());
        Long id = attendanceService.applyLeave(req);
        return Result.ok(id);
    }

    @Operation(summary = "月度考勤汇总统计")
    @GetMapping("/monthly-report")
    public Result<Map<String, Object>> monthlyReport(
            @Parameter(description = "月份（yyyy-MM格式），默认当前月") @RequestParam(value = "month", required = false) String month) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) return Result.unauthorized("请先登录");
        if (month == null || month.isEmpty()) {
            java.time.LocalDate now = java.time.LocalDate.now();
            month = String.format("%04d-%02d", now.getYear(), now.getMonthValue());
        }
        return Result.ok(attendanceService.getMonthlyReport(userId, month));
    }

    @Operation(summary = "月度每日考勤状态明细")
    @GetMapping("/daily-status")
    public Result<List<Map<String, Object>>> dailyStatus(
            @Parameter(description = "月份（yyyy-MM格式），默认当前月") @RequestParam(value = "month", required = false) String month) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) return Result.unauthorized("请先登录");
        if (month == null || month.isEmpty()) {
            java.time.LocalDate now = java.time.LocalDate.now();
            month = String.format("%04d-%02d", now.getYear(), now.getMonthValue());
        }
        return Result.ok(attendanceService.getDailyStatus(userId, month));
    }
}
