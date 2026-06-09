package com.oasystem.approval.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oasystem.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 审批模板实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("appr_template")
public class ApprovalTemplate extends BaseEntity {

    /** 模板名称（如"请假审批"、"报销审批"） */
    private String templateName;

    /** 模板编码 */
    private String templateCode;

    /** 模板描述 */
    private String description;

    /** 审批级数（如 3 级审批） */
    private Integer approvalLevels;

    /** 状态（0=禁用，1=启用） */
    private Integer status;
}
