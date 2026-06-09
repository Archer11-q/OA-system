package com.oasystem.schedule.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oasystem.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 日程实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sch_schedule")
public class Schedule extends BaseEntity {

    /** 日程标题 */
    private String title;

    /** 日程内容 */
    private String content;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 日程类型（1=个人日程，2=部门日程，3=会议） */
    private Integer scheduleType;

    /** 重要程度（1=普通，2=重要，3=紧急） */
    private Integer priority;

    /** 地点 */
    private String location;

    /** 创建人ID */
    private Long creatorId;

    /** 参与人ID列表（JSON数组存储） */
    private String participantIds;

    /** 状态（0=未开始，1=进行中，2=已完成，3=已取消） */
    private Integer status;
}
