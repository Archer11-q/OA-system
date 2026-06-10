package com.oasystem.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oasystem.approval.dto.StartApprovalDTO;
import com.oasystem.approval.entity.ApprovalTemplate;
import com.oasystem.approval.mapper.ApprovalTemplateMapper;
import com.oasystem.approval.service.ApprovalService;
import com.oasystem.attendance.entity.Attendance;
import com.oasystem.attendance.entity.LeaveRequest;
import com.oasystem.attendance.mapper.AttendanceMapper;
import com.oasystem.attendance.mapper.LeaveRequestMapper;
import com.oasystem.attendance.service.AttendanceService;
import com.oasystem.common.constant.Constants;
import com.oasystem.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 考勤管理 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceMapper attendanceMapper;
    private final LeaveRequestMapper leaveRequestMapper;
    private final ApprovalService approvalService;
    private final ApprovalTemplateMapper templateMapper;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // ==================== 签到 ====================

    @Override
    @Transactional
    public void signIn(Long userId, String location) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        Attendance att = attendanceMapper.selectOne(
                new LambdaQueryWrapper<Attendance>()
                        .eq(Attendance::getUserId, userId)
                        .eq(Attendance::getAttendanceDate, today)
        );

        if (att == null) {
            att = new Attendance();
            att.setUserId(userId);
            att.setAttendanceDate(today);
            att.setSignInTime(now);
            att.setSignInLocation(location);
            // 迟到判定：晚于上班时间
            if (now.isAfter(LocalTime.of(Constants.WORK_START_HOUR, 0))) {
                att.setStatus(Constants.ATTENDANCE_LATE);
                log.info("用户 {} 迟到签到, 时间={}", userId, now);
            } else {
                att.setStatus(Constants.ATTENDANCE_NORMAL);
            }
            attendanceMapper.insert(att);
        } else {
            // 已存在当天记录，更新签到信息
            att.setSignInTime(now);
            att.setSignInLocation(location);
            // 重新判定状态
            if (now.isAfter(LocalTime.of(Constants.WORK_START_HOUR, 0))) {
                if (!Constants.ATTENDANCE_ABSENT.equals(att.getStatus())) {
                    att.setStatus(Constants.ATTENDANCE_LATE);
                }
            } else {
                if (Constants.ATTENDANCE_ABSENT.equals(att.getStatus())) {
                    att.setStatus(Constants.ATTENDANCE_NORMAL);
                }
            }
            attendanceMapper.updateById(att);
        }
    }

    // ==================== 签退 ====================

    @Override
    @Transactional
    public void signOut(Long userId, String location) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        Attendance att = attendanceMapper.selectOne(
                new LambdaQueryWrapper<Attendance>()
                        .eq(Attendance::getUserId, userId)
                        .eq(Attendance::getAttendanceDate, today)
        );

        if (att == null) {
            // 无签到记录直接签退 → 缺勤
            att = new Attendance();
            att.setUserId(userId);
            att.setAttendanceDate(today);
            att.setSignOutTime(now);
            att.setSignOutLocation(location);
            att.setStatus(Constants.ATTENDANCE_ABSENT);
            attendanceMapper.insert(att);
        } else {
            att.setSignOutTime(now);
            att.setSignOutLocation(location);

            // 早退判定：早于下班时间 且 当前状态不是缺勤
            if (now.isBefore(LocalTime.of(Constants.WORK_END_HOUR, 0))
                    && !Constants.ATTENDANCE_ABSENT.equals(att.getStatus())) {
                // 如果之前已经是迟到，状态改为迟到+早退（仍用LATE或单独标记）
                // 简单处理：有早退则标记为EARLY（覆盖LATE，表示当天异常）
                if (Constants.ATTENDANCE_NORMAL.equals(att.getStatus())) {
                    att.setStatus(Constants.ATTENDANCE_EARLY);
                }
                // 如果已经是LATE，保持不变（已经是异常状态）
                log.info("用户 {} 早退签退, 时间={}", userId, now);
            }

            // 计算工作时长
            if (att.getSignInTime() != null) {
                double hours = Duration.between(att.getSignInTime(), now).toMinutes() / 60.0;
                att.setWorkHours((float) Math.round(hours * 10) / 10f); // 保留1位小数
            }

            attendanceMapper.updateById(att);
        }
    }

    // ==================== 考勤记录查询 ====================

    @Override
    public List<Attendance> getRecords(Long userId, String month) {
        // month 格式 YYYY-MM
        return attendanceMapper.selectByUserAndMonth(userId, month);
    }

    // ==================== 请假申请（集成审批中心） ====================

    @Override
    @Transactional
    public Long applyLeave(LeaveRequest req) {
        // 1. 保存请假申请
        req.setStatus(Constants.APPROVAL_PENDING);
        leaveRequestMapper.insert(req);
        log.info("请假申请已创建, leaveId={}, userId={}, days={}", req.getId(), req.getUserId(), req.getDays());

        // 2. 查找请假审批模板
        ApprovalTemplate template = templateMapper.selectOne(
                new LambdaQueryWrapper<ApprovalTemplate>()
                        .eq(ApprovalTemplate::getTemplateCode, "LEAVE_APPROVAL")
                        .eq(ApprovalTemplate::getStatus, Constants.STATUS_ENABLE)
        );
        if (template == null) {
            // 没有配置模板时仅保存请假记录，不创建审批实例
            log.warn("请假审批模板未配置，跳过创建审批实例, leaveId={}", req.getId());
            return req.getId();
        }

        // 3. 构建审批标题和内容
        String title = buildLeaveTitle(req);
        String content = buildLeaveContentJson(req);

        // 4. 创建审批实例
        StartApprovalDTO dto = new StartApprovalDTO();
        dto.setTemplateId(template.getId());
        dto.setTitle(title);
        dto.setContent(content);
        dto.setBusinessType(Constants.BUSINESS_TYPE_LEAVE);
        dto.setBusinessId(req.getId());

        Long instanceId = approvalService.start(dto, req.getUserId());

        // 5. 关联审批实例到请假申请
        req.setApprovalInstanceId(instanceId);
        leaveRequestMapper.updateById(req);

        log.info("请假审批实例已创建, leaveId={}, instanceId={}", req.getId(), instanceId);
        return req.getId();
    }

    // ==================== 月度汇总统计 ====================

    @Override
    public Map<String, Object> getMonthlyReport(Long userId, String month) {
        List<Attendance> records = getMonthRecords(userId, month);

        int totalDays = records.size();
        int normalDays = 0, lateDays = 0, earlyDays = 0, absentDays = 0;
        double totalWorkHours = 0;

        for (Attendance att : records) {
            String status = att.getStatus();
            if (Constants.ATTENDANCE_NORMAL.equals(status)) {
                normalDays++;
            } else if (Constants.ATTENDANCE_LATE.equals(status)) {
                lateDays++;
            } else if (Constants.ATTENDANCE_EARLY.equals(status)) {
                earlyDays++;
            } else if (Constants.ATTENDANCE_ABSENT.equals(status)) {
                absentDays++;
            }
            if (att.getWorkHours() != null) {
                totalWorkHours += att.getWorkHours();
            }
        }

        // 统计请假天数
        YearMonth ym = YearMonth.parse(month);
        long leaveDays = leaveRequestMapper.selectCount(
                new LambdaQueryWrapper<LeaveRequest>()
                        .eq(LeaveRequest::getUserId, userId)
                        .eq(LeaveRequest::getStatus, Constants.APPROVAL_APPROVED)
                        .ge(LeaveRequest::getStartDate, ym.atDay(1))
                        .le(LeaveRequest::getEndDate, ym.atEndOfMonth())
        );

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("month", month);
        report.put("userId", userId);
        report.put("totalDays", totalDays);
        report.put("normalDays", normalDays);
        report.put("lateDays", lateDays);
        report.put("earlyDays", earlyDays);
        report.put("absentDays", absentDays);
        report.put("leaveDays", leaveDays);
        report.put("totalWorkHours", Math.round(totalWorkHours * 10) / 10.0);
        return report;
    }

    // ==================== 每日状态明细 ====================

    @Override
    public List<Map<String, Object>> getDailyStatus(Long userId, String month) {
        List<Attendance> records = getMonthRecords(userId, month);
        Map<LocalDate, Attendance> recordMap = records.stream()
                .collect(Collectors.toMap(Attendance::getAttendanceDate, a -> a, (a, b) -> a));

        YearMonth ym = YearMonth.parse(month);
        int daysInMonth = ym.lengthOfMonth();

        List<Map<String, Object>> result = new ArrayList<>();
        for (int d = 1; d <= daysInMonth; d++) {
            LocalDate date = ym.atDay(d);
            Map<String, Object> dayInfo = new LinkedHashMap<>();
            dayInfo.put("date", date.toString());
            dayInfo.put("dayOfWeek", date.getDayOfWeek().getValue()); // 1=周一,7=周日

            Attendance att = recordMap.get(date);
            if (att != null) {
                dayInfo.put("status", att.getStatus());
                dayInfo.put("signInTime", att.getSignInTime() != null ? att.getSignInTime().toString() : null);
                dayInfo.put("signOutTime", att.getSignOutTime() != null ? att.getSignOutTime().toString() : null);
                dayInfo.put("workHours", att.getWorkHours());
            } else {
                // 周末不标记为缺勤
                boolean isWeekend = date.getDayOfWeek().getValue() >= 6;
                dayInfo.put("status", isWeekend ? "WEEKEND" : Constants.ATTENDANCE_ABSENT);
                dayInfo.put("signInTime", null);
                dayInfo.put("signOutTime", null);
                dayInfo.put("workHours", null);
            }
            result.add(dayInfo);
        }
        return result;
    }

    // ==================== 请假审批辅助方法 ====================

    /**
     * 构建审批标题
     */
    private String buildLeaveTitle(LeaveRequest req) {
        String typeName = switch (req.getLeaveType()) {
            case Constants.LEAVE_ANNUAL -> "年假";
            case Constants.LEAVE_PERSONAL -> "事假";
            case Constants.LEAVE_SICK -> "病假";
            case Constants.LEAVE_COMPENSATORY -> "调休";
            default -> "其他";
        };
        return typeName + "申请 - " + req.getDays() + "天";
    }

    /**
     * 构建审批内容 JSON
     */
    private String buildLeaveContentJson(LeaveRequest req) {
        try {
            Map<String, Object> content = new LinkedHashMap<>();
            content.put("leaveType", req.getLeaveType());
            content.put("leaveTypeName", getLeaveTypeName(req.getLeaveType()));
            content.put("startDate", req.getStartDate().toString());
            content.put("endDate", req.getEndDate().toString());
            content.put("days", req.getDays());
            content.put("reason", req.getReason() != null ? req.getReason() : "");
            return OBJECT_MAPPER.writeValueAsString(content);
        } catch (Exception e) {
            log.error("构建请假审批内容JSON失败", e);
            return "{}";
        }
    }

    /**
     * 获取请假类型名称
     */
    private String getLeaveTypeName(Integer leaveType) {
        return switch (leaveType) {
            case Constants.LEAVE_ANNUAL -> "年假";
            case Constants.LEAVE_PERSONAL -> "事假";
            case Constants.LEAVE_SICK -> "病假";
            case Constants.LEAVE_COMPENSATORY -> "调休";
            default -> "其他";
        };
    }

    // ==================== 私有方法 ====================

    /**
     * 按用户和月份查询考勤记录
     */
    private List<Attendance> getMonthRecords(Long userId, String month) {
        YearMonth ym = YearMonth.parse(month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        return attendanceMapper.selectList(
                new LambdaQueryWrapper<Attendance>()
                        .eq(Attendance::getUserId, userId)
                        .ge(Attendance::getAttendanceDate, start)
                        .le(Attendance::getAttendanceDate, end)
                        .orderByAsc(Attendance::getAttendanceDate)
        );
    }
}
