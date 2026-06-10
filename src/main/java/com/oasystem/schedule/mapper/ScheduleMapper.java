package com.oasystem.schedule.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oasystem.schedule.entity.Schedule;
import org.apache.ibatis.annotations.Mapper;

/**
 * 日程 Mapper
 */
@Mapper
public interface ScheduleMapper extends BaseMapper<Schedule> {
}
