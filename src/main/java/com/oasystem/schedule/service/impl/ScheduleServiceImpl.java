package com.oasystem.schedule.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oasystem.common.exception.BusinessException;
import com.oasystem.schedule.entity.Schedule;
import com.oasystem.schedule.mapper.ScheduleMapper;
import com.oasystem.schedule.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 日程管理 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleMapper scheduleMapper;

    @Override
    public List<Schedule> listByDateRange(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        // 查询个人日程 + 部门日程 + 会议
        // 当前用户创建的 或 参与人包含当前用户的
        wrapper.and(w -> w
                .eq(Schedule::getCreatorId, userId)
                .or()
                .like(Schedule::getParticipantIds, String.valueOf(userId))
        );
        // 日期范围过滤
        if (startTime != null) {
            wrapper.ge(Schedule::getEndTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(Schedule::getStartTime, endTime);
        }
        wrapper.orderByAsc(Schedule::getStartTime);
        return scheduleMapper.selectList(wrapper);
    }

    @Override
    public Schedule getById(Long id) {
        return scheduleMapper.selectById(id);
    }

    @Override
    public void createSchedule(Schedule schedule) {
        if (schedule.getStartTime() != null && schedule.getEndTime() != null
                && schedule.getStartTime().isAfter(schedule.getEndTime())) {
            throw new BusinessException("开始时间不能晚于结束时间");
        }
        if (schedule.getStatus() == null) {
            schedule.setStatus(0); // 默认未开始
        }
        scheduleMapper.insert(schedule);
        log.info("日程创建成功, id={}, title={}", schedule.getId(), schedule.getTitle());
    }

    @Override
    public void updateSchedule(Schedule schedule) {
        Schedule exist = scheduleMapper.selectById(schedule.getId());
        if (exist == null) {
            throw new BusinessException("日程不存在");
        }
        if (schedule.getStartTime() != null && schedule.getEndTime() != null
                && schedule.getStartTime().isAfter(schedule.getEndTime())) {
            throw new BusinessException("开始时间不能晚于结束时间");
        }
        scheduleMapper.updateById(schedule);
        log.info("日程更新成功, id={}", schedule.getId());
    }

    @Override
    public void deleteSchedule(Long id, Long userId) {
        Schedule exist = scheduleMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException("日程不存在");
        }
        if (!exist.getCreatorId().equals(userId)) {
            throw new BusinessException("只能删除自己创建的日程");
        }
        scheduleMapper.deleteById(id);
        log.info("日程删除成功, id={}", id);
    }
}
