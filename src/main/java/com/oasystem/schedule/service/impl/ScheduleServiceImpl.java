package com.oasystem.schedule.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oasystem.common.exception.BusinessException;
import com.oasystem.schedule.entity.Schedule;
import com.oasystem.schedule.mapper.ScheduleMapper;
import com.oasystem.schedule.service.ScheduleService;
import com.oasystem.system.entity.User;
import com.oasystem.system.mapper.UserMapper;
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
    private final UserMapper userMapper;

    /** 可见性常量 */
    private static final int VISIBILITY_PRIVATE = 1;
    private static final int VISIBILITY_DEPT = 2;
    private static final int VISIBILITY_PUBLIC = 3;

    @Override
    public List<Schedule> listByDateRange(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        // 获取当前用户的部门ID
        final Long userDeptId = getDeptId(userId);

        // 获取同部门用户ID列表（提前计算，避免 lambda 内的 effectively final 问题）
        final List<Long> deptUserIds = (userDeptId != null)
            ? userMapper.selectList(
                new LambdaQueryWrapper<User>().eq(User::getDeptId, userDeptId)
              ).stream().map(User::getId).toList()
            : java.util.Collections.emptyList();

        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();

        // 可见性规则：
        // 1. 自己创建的日程（任何可见性）
        // 2. 自己是参与人的日程
        // 3. 公开可见（visibility=3）的日程
        // 4. 同部门可见（visibility=2 且同部门创建人）的日程
        wrapper.and(w -> {
            // 自己创建的
            w.eq(Schedule::getCreatorId, userId);
            // 参与人包含
            w.or().like(Schedule::getParticipantIds, String.valueOf(userId));
            // 公开
            w.or().eq(Schedule::getVisibility, VISIBILITY_PUBLIC);
            // 部门可见（需同部门）
            if (!deptUserIds.isEmpty()) {
                w.or().and(w2 -> w2
                    .eq(Schedule::getVisibility, VISIBILITY_DEPT)
                    .in(Schedule::getCreatorId, deptUserIds)
                );
            }
        });

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

    /**
     * 根据用户ID获取其部门ID
     */
    private Long getDeptId(Long userId) {
        if (userId == null) return null;
        User user = userMapper.selectById(userId);
        return user != null ? user.getDeptId() : null;
    }

    @Override
    public List<Schedule> getUpcomingReminders(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next24h = now.plusHours(24);

        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        // 用户创建的 或 参与人包含当前用户的
        wrapper.and(w -> w
                .eq(Schedule::getCreatorId, userId)
                .or()
                .like(Schedule::getParticipantIds, String.valueOf(userId))
        );
        // 设置了提醒时间
        wrapper.isNotNull(Schedule::getReminderMinutes);
        // 提醒还未发送
        wrapper.eq(Schedule::getReminderSent, 0);
        // 日程开始时间在未来24小时内
        wrapper.ge(Schedule::getStartTime, now);
        wrapper.le(Schedule::getStartTime, next24h);
        // 日程未取消
        wrapper.ne(Schedule::getStatus, 3);
        // 按开始时间排序
        wrapper.orderByAsc(Schedule::getStartTime);

        return scheduleMapper.selectList(wrapper);
    }
}
