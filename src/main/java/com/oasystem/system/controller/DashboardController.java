package com.oasystem.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oasystem.approval.entity.ApprovalInstance;
import com.oasystem.approval.mapper.ApprovalInstanceMapper;
import com.oasystem.attendance.entity.Attendance;
import com.oasystem.attendance.mapper.AttendanceMapper;
import com.oasystem.common.Result;
import com.oasystem.common.constant.Constants;
import com.oasystem.expense.entity.ExpenseRequest;
import com.oasystem.expense.mapper.ExpenseMapper;
import com.oasystem.notice.mapper.NoticeMapper;
import com.oasystem.system.mapper.DeptMapper;
import com.oasystem.system.mapper.RoleMapper;
import com.oasystem.system.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

/**
 * 数据看板 Controller
 * <p>
 * 提供首页数据看板所需的聚合统计数据。
 */
@Tag(name = "数据看板", description = "首页聚合统计数据")
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UserMapper userMapper;
    private final DeptMapper deptMapper;
    private final RoleMapper roleMapper;
    private final AttendanceMapper attendanceMapper;
    private final ApprovalInstanceMapper approvalInstanceMapper;
    private final ExpenseMapper expenseMapper;
    private final NoticeMapper noticeMapper;

    @Operation(summary = "数据看板总览")
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        Map<String, Object> data = new LinkedHashMap<>();

        // === 系统概览 ===
        Map<String, Object> system = new LinkedHashMap<>();
        system.put("userCount", userMapper.selectCount(null));
        system.put("deptCount", deptMapper.selectCount(null));
        system.put("roleCount", roleMapper.selectCount(null));
        system.put("noticeCount", noticeMapper.selectCount(
                new LambdaQueryWrapper<com.oasystem.notice.entity.Notice>()
                        .eq(com.oasystem.notice.entity.Notice::getStatus, 1)
        ));
        data.put("system", system);

        // === 今日考勤 ===
        LocalDate today = LocalDate.now();
        Map<String, Object> todayAtt = new LinkedHashMap<>();
        List<Attendance> todayRecords = attendanceMapper.selectList(
                new LambdaQueryWrapper<Attendance>()
                        .eq(Attendance::getAttendanceDate, today)
        );
        todayAtt.put("total", todayRecords.size());
        todayAtt.put("normal", countByStatus(todayRecords, Constants.ATTENDANCE_NORMAL));
        todayAtt.put("late", countByStatus(todayRecords, Constants.ATTENDANCE_LATE));
        todayAtt.put("early", countByStatus(todayRecords, Constants.ATTENDANCE_EARLY));
        todayAtt.put("absent", countByStatus(todayRecords, Constants.ATTENDANCE_ABSENT));
        data.put("todayAttendance", todayAtt);

        // === 本月审批 ===
        YearMonth thisMonth = YearMonth.now();
        List<ApprovalInstance> monthApprovals = approvalInstanceMapper.selectList(
                new LambdaQueryWrapper<ApprovalInstance>()
                        .ge(ApprovalInstance::getCreateTime, thisMonth.atDay(1).atStartOfDay())
                        .le(ApprovalInstance::getCreateTime, thisMonth.atEndOfMonth().atTime(23, 59, 59))
        );
        Map<String, Object> approval = new LinkedHashMap<>();
        approval.put("total", monthApprovals.size());
        approval.put("pending", countApprovalByStatus(monthApprovals, Constants.APPROVAL_PENDING));
        approval.put("approved", countApprovalByStatus(monthApprovals, Constants.APPROVAL_APPROVED));
        approval.put("rejected", countApprovalByStatus(monthApprovals, Constants.APPROVAL_REJECTED));
        data.put("monthlyApproval", approval);

        // === 本月报销 ===
        List<ExpenseRequest> monthExpenses = expenseMapper.selectList(
                new LambdaQueryWrapper<ExpenseRequest>()
                        .ge(ExpenseRequest::getCreateTime, thisMonth.atDay(1).atStartOfDay())
                        .le(ExpenseRequest::getCreateTime, thisMonth.atEndOfMonth().atTime(23, 59, 59))
        );
        Map<String, Object> expense = new LinkedHashMap<>();
        expense.put("totalCount", monthExpenses.size());
        BigDecimal totalAmount = monthExpenses.stream()
                .map(e -> e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        expense.put("totalAmount", totalAmount);
        expense.put("approvedAmount", monthExpenses.stream()
                .filter(e -> Constants.APPROVAL_APPROVED.equals(e.getStatus()))
                .map(e -> e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        data.put("monthlyExpense", expense);

        return Result.ok(data);
    }

    @Operation(summary = "近7天考勤趋势")
    @GetMapping("/attendance-trend")
    public Result<List<Map<String, Object>>> attendanceTrend() {
        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            List<Attendance> dayRecords = attendanceMapper.selectList(
                    new LambdaQueryWrapper<Attendance>()
                            .eq(Attendance::getAttendanceDate, date)
            );

            Map<String, Object> day = new LinkedHashMap<>();
            day.put("date", date.toString());
            day.put("dayOfWeek", date.getDayOfWeek().getValue());
            day.put("total", dayRecords.size());
            day.put("normal", countByStatus(dayRecords, Constants.ATTENDANCE_NORMAL));
            day.put("late", countByStatus(dayRecords, Constants.ATTENDANCE_LATE));
            day.put("early", countByStatus(dayRecords, Constants.ATTENDANCE_EARLY));
            day.put("absent", countByStatus(dayRecords, Constants.ATTENDANCE_ABSENT));
            trend.add(day);
        }

        return Result.ok(trend);
    }

    @Operation(summary = "审批状态分布（饼图数据）")
    @GetMapping("/approval-distribution")
    public Result<List<Map<String, Object>>> approvalDistribution() {
        YearMonth thisMonth = YearMonth.now();
        List<ApprovalInstance> monthApprovals = approvalInstanceMapper.selectList(
                new LambdaQueryWrapper<ApprovalInstance>()
                        .ge(ApprovalInstance::getCreateTime, thisMonth.atDay(1).atStartOfDay())
                        .le(ApprovalInstance::getCreateTime, thisMonth.atEndOfMonth().atTime(23, 59, 59))
        );

        List<Map<String, Object>> distribution = new ArrayList<>();
        distribution.add(pieItem("审批中", countApprovalByStatus(monthApprovals, Constants.APPROVAL_PENDING)));
        distribution.add(pieItem("已通过", countApprovalByStatus(monthApprovals, Constants.APPROVAL_APPROVED)));
        distribution.add(pieItem("已驳回", countApprovalByStatus(monthApprovals, Constants.APPROVAL_REJECTED)));

        return Result.ok(distribution);
    }

    @Operation(summary = "报销类型分布（饼图数据）")
    @GetMapping("/expense-distribution")
    public Result<List<Map<String, Object>>> expenseDistribution() {
        YearMonth thisMonth = YearMonth.now();
        List<ExpenseRequest> monthExpenses = expenseMapper.selectList(
                new LambdaQueryWrapper<ExpenseRequest>()
                        .ge(ExpenseRequest::getCreateTime, thisMonth.atDay(1).atStartOfDay())
                        .le(ExpenseRequest::getCreateTime, thisMonth.atEndOfMonth().atTime(23, 59, 59))
        );

        // 按类型分组统计
        Map<Integer, BigDecimal> typeAmount = new LinkedHashMap<>();
        Map<Integer, Integer> typeCount = new LinkedHashMap<>();
        String[] typeNames = {"", "差旅费", "办公费", "招待费", "交通费", "其他"};

        for (ExpenseRequest e : monthExpenses) {
            Integer t = e.getExpenseType() != null ? e.getExpenseType() : 5;
            typeAmount.merge(t, e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO, BigDecimal::add);
            typeCount.merge(t, 1, Integer::sum);
        }

        List<Map<String, Object>> distribution = new ArrayList<>();
        for (int t = 1; t <= 5; t++) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", t);
            item.put("name", t < typeNames.length ? typeNames[t] : "其他");
            item.put("count", typeCount.getOrDefault(t, 0));
            item.put("amount", typeAmount.getOrDefault(t, BigDecimal.ZERO));
            distribution.add(item);
        }

        return Result.ok(distribution);
    }

    // ==================== 私有方法 ====================

    private long countByStatus(List<Attendance> records, String status) {
        return records.stream().filter(r -> status.equals(r.getStatus())).count();
    }

    private long countApprovalByStatus(List<ApprovalInstance> instances, Integer status) {
        return instances.stream().filter(i -> status.equals(i.getStatus())).count();
    }

    private Map<String, Object> pieItem(String name, long value) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", name);
        item.put("value", value);
        return item;
    }
}
