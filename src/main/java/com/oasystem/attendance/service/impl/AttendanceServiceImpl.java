package com.oasystem.attendance.service.impl;

import com.oasystem.attendance.entity.Attendance;
import com.oasystem.attendance.entity.LeaveRequest;
import com.oasystem.attendance.mapper.AttendanceMapper;
import com.oasystem.attendance.mapper.LeaveRequestMapper;
import com.oasystem.attendance.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceMapper attendanceMapper;
    private final LeaveRequestMapper leaveRequestMapper;

    @Override
    @Transactional
    public void signIn(Long userId, String location) {
        LocalDate today = LocalDate.now();
        Attendance att = attendanceMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Attendance>()
                .eq(Attendance::getUserId, userId)
                .eq(Attendance::getAttendanceDate, today));
        if (att == null) {
            att = new Attendance();
            att.setUserId(userId);
            att.setAttendanceDate(today);
            att.setSignInTime(LocalTime.now());
            att.setSignInLocation(location);
            att.setStatus("NORMAL");
            attendanceMapper.insert(att);
        } else {
            // 已存在当天记录，更新签到信息
            att.setSignInTime(LocalTime.now());
            att.setSignInLocation(location);
            attendanceMapper.updateById(att);
        }
    }

    @Override
    @Transactional
    public void signOut(Long userId, String location) {
        LocalDate today = LocalDate.now();
        Attendance att = attendanceMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Attendance>()
                .eq(Attendance::getUserId, userId)
                .eq(Attendance::getAttendanceDate, today));
        if (att == null) {
            att = new Attendance();
            att.setUserId(userId);
            att.setAttendanceDate(today);
            att.setSignOutTime(LocalTime.now());
            att.setSignOutLocation(location);
            att.setStatus("ABSENT");
            attendanceMapper.insert(att);
        } else {
            att.setSignOutTime(LocalTime.now());
            att.setSignOutLocation(location);
            // 计算工作时长（简单计算）
            if (att.getSignInTime() != null && att.getSignOutTime() != null) {
                double hours = java.time.Duration.between(att.getSignInTime(), att.getSignOutTime()).toMinutes() / 60.0;
                att.setWorkHours((float) hours);
            }
            attendanceMapper.updateById(att);
        }
    }

    @Override
    public List<Attendance> getRecords(Long userId, String month) {
        // month 格式 YYYY-MM
        return attendanceMapper.selectByUserAndMonth(userId, month);
    }

    @Override
    @Transactional
    public Long applyLeave(LeaveRequest req) {
        leaveRequestMapper.insert(req);
        return req.getId();
    }
}

