package com.oasystem.attendance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oasystem.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 考勤记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("att_record")
public class Attendance extends BaseEntity {

    /** 用户ID */
    private Long userId;

    /** 考勤日期 */
    private LocalDate attendanceDate;

    /** 签到时间 */
    private LocalTime signInTime;

    /** 签退时间 */
    private LocalTime signOutTime;

    /** 签到状态（NORMAL/LATE/EARLY/ABSENT） */
    private String status;

    /** 签到类型（1=正常，2=外勤，3=补卡） */
    private Integer signInType;

    /** 签退类型（1=正常，2=外勤，3=补卡） */
    private Integer signOutType;

    /** 工作时长（小时） */
    private Float workHours;

    /** 签到地点 */
    private String signInLocation;

    /** 签退地点 */
    private String signOutLocation;

    /** 备注 */
    private String remark;
}
