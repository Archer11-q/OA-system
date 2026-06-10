package com.oasystem.approval.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oasystem.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 审批实例实体（一条具体的审批申请）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("appr_instance")
public class ApprovalInstance extends BaseEntity {

    /** 审批模板ID */
    private Long templateId;

    /** 申请人ID */
    private Long applicantId;

    /** 审批标题 */
    private String title;

    /** 审批内容（JSON格式存储表单数据） */
    private String content;

    /** 总审批级数 */
    private Integer totalLevels;

    /** 当前审批级别 */
    private Integer currentLevel;

    /** 状态（0=审批中，1=已通过，2=已驳回，3=已撤回） */
    private Integer status;

    /** 审批人快照（JSON数组，启动时解析的审批人ID列表） */
    private String approversSnapshot;

    /** 完成时间 */
    private LocalDateTime finishTime;
}
