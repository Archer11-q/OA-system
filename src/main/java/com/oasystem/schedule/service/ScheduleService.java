package com.oasystem.schedule.service;

import com.oasystem.schedule.entity.Schedule;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 日程管理 Service 接口
 */
public interface ScheduleService {

    /**
     * 按日期范围查询日程列表
     *
     * @param userId    当前用户ID（查询个人日程 + 参与的日程）
     * @param startTime 开始时间（可选）
     * @param endTime   结束时间（可选）
     * @return 日程列表
     */
    List<Schedule> listByDateRange(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据ID查询日程详情
     */
    Schedule getById(Long id);

    /**
     * 创建日程
     */
    void createSchedule(Schedule schedule);

    /**
     * 更新日程（仅创建人可更新）
     */
    void updateSchedule(Schedule schedule);

    /**
     * 删除日程（仅创建人可删除）
     */
    void deleteSchedule(Long id, Long userId);

    /**
     * 获取用户即将到来的日程提醒（未来24小时内、未发送提醒的日程）
     */
    List<Schedule> getUpcomingReminders(Long userId);
}
