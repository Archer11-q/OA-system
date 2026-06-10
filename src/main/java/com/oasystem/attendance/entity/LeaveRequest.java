package com.oasystem.attendance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oasystem.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 请假申请实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("att_leave_request")
public class LeaveRequest extends BaseEntity {

    /** 申请人ID */
    private Long userId;

    /** 请假类型（1=年假，2=事假，3=病假，4=调休） */
    private Integer leaveType;

    /** 开始日期 */
    private LocalDate startDate;

    /** 结束日期 */
    private LocalDate endDate;

    /** 请假天数 */
    private Float days;

    /** 请假原因 */
    private String reason;

    /** 状态（0=审批中，1=已通过，2=已驳回，3=已撤回） */
    private Integer status;

    /** 审批意见 */
    private String approvalComment;

    /** 审批人ID */
    private Long approverId;

    /** 审批时间 */
    private LocalDateTime approvalTime;

    /** 关联审批实例ID */
    private Long approvalInstanceId;
}
