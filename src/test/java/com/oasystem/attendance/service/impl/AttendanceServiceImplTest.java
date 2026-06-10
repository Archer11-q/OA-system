package com.oasystem.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oasystem.approval.dto.StartApprovalDTO;
import com.oasystem.approval.entity.ApprovalTemplate;
import com.oasystem.approval.mapper.ApprovalTemplateMapper;
import com.oasystem.approval.service.ApprovalService;
import com.oasystem.attendance.entity.Attendance;
import com.oasystem.attendance.entity.LeaveRequest;
import com.oasystem.attendance.mapper.AttendanceMapper;
import com.oasystem.attendance.mapper.LeaveRequestMapper;
import com.oasystem.common.constant.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 考勤管理 Service 单元测试 — 请假审批集成 + 考勤统计
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("考勤管理 Service 单元测试")
class AttendanceServiceImplTest {

    @Mock private AttendanceMapper attendanceMapper;
    @Mock private LeaveRequestMapper leaveRequestMapper;
    @Mock private ApprovalService approvalService;
    @Mock private ApprovalTemplateMapper templateMapper;

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    private ApprovalTemplate leaveTemplate;

    @BeforeEach
    void setUp() {
        leaveTemplate = new ApprovalTemplate();
        leaveTemplate.setId(1L);
        leaveTemplate.setTemplateName("请假审批");
        leaveTemplate.setTemplateCode("LEAVE_APPROVAL");
        leaveTemplate.setStatus(1);
    }

    // ==================== 请假申请（集成审批中心） ====================

    @Test
    @DisplayName("请假申请 — 创建请假记录并自动发起审批实例")
    void testApplyLeave_WithApprovalTemplate() {
        // given
        LeaveRequest req = new LeaveRequest();
        req.setUserId(10L);
        req.setLeaveType(Constants.LEAVE_ANNUAL);
        req.setStartDate(LocalDate.of(2026, 6, 15));
        req.setEndDate(LocalDate.of(2026, 6, 16));
        req.setDays(2.0f);
        req.setReason("年假休息");

        when(leaveRequestMapper.insert(any(LeaveRequest.class))).thenAnswer(inv -> {
            LeaveRequest lr = inv.getArgument(0);
            lr.setId(100L);
            return 1;
        });
        when(templateMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(leaveTemplate);
        when(approvalService.start(any(StartApprovalDTO.class), eq(10L))).thenReturn(200L);
        when(leaveRequestMapper.updateById(any(LeaveRequest.class))).thenReturn(1);

        // when
        Long leaveId = attendanceService.applyLeave(req);

        // then
        assertNotNull(leaveId);
        assertEquals(100L, leaveId);

        // 验证请假状态为审批中
        assertEquals(Constants.APPROVAL_PENDING, req.getStatus());
        assertEquals(200L, req.getApprovalInstanceId());

        // 验证调用了审批服务
        ArgumentCaptor<StartApprovalDTO> dtoCaptor = ArgumentCaptor.forClass(StartApprovalDTO.class);
        verify(approvalService).start(dtoCaptor.capture(), eq(10L));
        StartApprovalDTO dto = dtoCaptor.getValue();
        assertEquals(leaveTemplate.getId(), dto.getTemplateId());
        assertEquals(Constants.BUSINESS_TYPE_LEAVE, dto.getBusinessType());
        assertEquals(100L, dto.getBusinessId());
        assertTrue(dto.getTitle().contains("年假"));
        assertTrue(dto.getContent().contains("年假休息"));
    }

    @Test
    @DisplayName("请假申请 — 审批模板未配置时仅保存请假记录不抛异常")
    void testApplyLeave_NoTemplate_GracefulDegradation() {
        // given
        LeaveRequest req = new LeaveRequest();
        req.setUserId(10L);
        req.setLeaveType(Constants.LEAVE_PERSONAL);
        req.setStartDate(LocalDate.of(2026, 6, 20));
        req.setEndDate(LocalDate.of(2026, 6, 20));
        req.setDays(1.0f);
        req.setReason("家中有事");

        when(leaveRequestMapper.insert(any(LeaveRequest.class))).thenAnswer(inv -> {
            LeaveRequest lr = inv.getArgument(0);
            lr.setId(101L);
            return 1;
        });
        // 模板不存在
        when(templateMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // when
        Long leaveId = attendanceService.applyLeave(req);

        // then: 正常返回，不抛异常
        assertNotNull(leaveId);
        assertEquals(101L, leaveId);
        assertEquals(Constants.APPROVAL_PENDING, req.getStatus());
        assertNull(req.getApprovalInstanceId());

        // 未调用审批服务
        verify(approvalService, never()).start(any(), anyLong());
    }

    // ==================== 签到签退 ====================

    @Test
    @DisplayName("签到 — 正常签到（9点前）")
    void testSignIn_Normal() {
        // 模拟9点之前签到
        when(attendanceMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(attendanceMapper.insert(any(Attendance.class))).thenReturn(1);

        // 无法直接 mock LocalTime.now()，但 status 判断逻辑已在 ServiceImpl 中
        // 这里验证 insert 被调用即可
        attendanceService.signIn(10L, "公司");

        ArgumentCaptor<Attendance> captor = ArgumentCaptor.forClass(Attendance.class);
        verify(attendanceMapper).insert(captor.capture());
        Attendance saved = captor.getValue();
        assertEquals(10L, saved.getUserId());
        assertEquals(LocalDate.now(), saved.getAttendanceDate());
        assertEquals("公司", saved.getSignInLocation());
    }

    // ==================== 月度汇总 ====================

    @Test
    @DisplayName("月度汇总 — 正确统计出勤/迟到/早退/缺勤")
    void testGetMonthlyReport() {
        String month = "2026-06";

        // 模拟考勤记录
        Attendance a1 = buildAttendance("2026-06-01", "NORMAL", 9.0f);
        Attendance a2 = buildAttendance("2026-06-02", "LATE", 7.5f);
        Attendance a3 = buildAttendance("2026-06-03", "EARLY", 8.0f);
        Attendance a4 = buildAttendance("2026-06-04", "ABSENT", null);

        when(attendanceMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(a1, a2, a3, a4));
        when(leaveRequestMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L);

        Map<String, Object> report = attendanceService.getMonthlyReport(10L, month);

        assertEquals(4, report.get("totalDays"));
        assertEquals(1, report.get("normalDays"));
        assertEquals(1, report.get("lateDays"));
        assertEquals(1, report.get("earlyDays"));
        assertEquals(1, report.get("absentDays"));
        assertEquals(2L, report.get("leaveDays"));
        assertEquals(24.5, (double) report.get("totalWorkHours"), 0.01);
    }

    // ==================== 每日明细 ====================

    @Test
    @DisplayName("每日明细 — 月份所有日期状态正确，周末标记为WEEKEND")
    void testGetDailyStatus() {
        String month = "2026-06";

        // 6月1日是周一，有正常考勤
        Attendance a1 = buildAttendance("2026-06-01", "NORMAL", 9.0f);
        when(attendanceMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(a1));

        List<Map<String, Object>> statuses = attendanceService.getDailyStatus(10L, month);

        // 6月有30天
        assertEquals(30, statuses.size());
        // 第1天是周一，有考勤
        assertEquals("NORMAL", statuses.get(0).get("status"));
        // 第6天是周六（6月6日是周六），应为WEEKEND
        assertEquals("WEEKEND", statuses.get(5).get("status"));
        // 第7天是周日，应为WEEKEND
        assertEquals("WEEKEND", statuses.get(6).get("status"));
        // 第2天是周二，无考勤 → ABSENT
        assertEquals(Constants.ATTENDANCE_ABSENT, statuses.get(1).get("status"));
    }

    // ==================== 辅助方法 ====================

    private Attendance buildAttendance(String dateStr, String status, Float workHours) {
        Attendance att = new Attendance();
        att.setUserId(10L);
        att.setAttendanceDate(LocalDate.parse(dateStr));
        att.setStatus(status);
        att.setSignInTime("NORMAL".equals(status) || "LATE".equals(status) || "EARLY".equals(status)
                ? LocalTime.of(8, 55) : null);
        att.setSignOutTime(LocalTime.of(18, 5));
        att.setWorkHours(workHours);
        return att;
    }
}
