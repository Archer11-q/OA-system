package com.oasystem.attendance.service;

import com.oasystem.attendance.entity.Attendance;
import com.oasystem.attendance.entity.LeaveRequest;

import java.util.List;
import java.util.Map;

/**
 * 考勤管理 Service 接口
 */
public interface AttendanceService {

    /**
     * 签到
     */
    void signIn(Long userId, String location);

    /**
     * 签退
     */
    void signOut(Long userId, String location);

    /**
     * 查询考勤记录（按月）
     */
    List<Attendance> getRecords(Long userId, String month);

    /**
     * 申请请假
     */
    Long applyLeave(LeaveRequest req);

    /**
     * 月度考勤汇总统计
     *
     * @param userId 用户ID
     * @param month  月份（YYYY-MM）
     * @return 统计结果（totalDays/normalDays/lateDays/earlyDays/absentDays/totalWorkHours）
     */
    Map<String, Object> getMonthlyReport(Long userId, String month);

    /**
     * 月度每日考勤状态明细
     *
     * @param userId 用户ID
     * @param month  月份（YYYY-MM）
     * @return 每日状态列表
     */
    List<Map<String, Object>> getDailyStatus(Long userId, String month);
}
