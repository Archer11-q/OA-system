package com.oasystem.expense.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oasystem.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 报销申请实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exp_request")
public class ExpenseRequest extends BaseEntity {

    /** 申请人ID */
    private Long userId;

    /** 报销标题 */
    private String title;

    /** 报销类型（1=差旅费，2=办公费，3=招待费，4=交通费，5=其他） */
    private Integer expenseType;

    /** 报销金额 */
    private BigDecimal amount;

    /** 报销说明 */
    private String description;

    /** 附件URL列表（JSON数组） */
    private String attachments;

    /** 状态（0=审批中，1=已通过，2=已驳回，3=已撤回） */
    private Integer status;

    /** 审批人ID */
    private Long approverId;

    /** 审批意见 */
    private String approvalComment;

    /** 审批时间 */
    private LocalDateTime approvalTime;

    /** 关联审批实例ID */
    private Long approvalInstanceId;
}
