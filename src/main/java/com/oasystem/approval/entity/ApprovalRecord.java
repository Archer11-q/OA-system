package com.oasystem.approval.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oasystem.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 审批记录实体（每一级审批的意见）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("appr_record")
public class ApprovalRecord extends BaseEntity {

    /** 审批实例ID */
    private Long instanceId;

    /** 审批级别（第几级审批） */
    private Integer level;

    /** 审批人ID */
    private Long approverId;

    /** 审批结果（0=待审批，1=同意，2=驳回） */
    private Integer result;

    /** 审批意见 */
    private String comment;

    /** 审批时间 */
    private java.time.LocalDateTime approvalTime;
}
