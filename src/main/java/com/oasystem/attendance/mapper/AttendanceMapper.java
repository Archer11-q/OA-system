package com.oasystem.attendance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oasystem.attendance.entity.Attendance;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AttendanceMapper extends BaseMapper<Attendance> {
    List<Attendance> selectByUserAndMonth(Long userId, String month);
}

