package com.oasystem.approval.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发起审批请求 DTO
 */
@Data
public class StartApprovalDTO {

    /** 审批模板ID */
    @NotNull(message = "审批模板不能为空")
    private Long templateId;

    /** 审批标题 */
    @NotBlank(message = "审批标题不能为空")
    private String title;

    /** 审批内容（JSON格式存储表单数据） */
    private String content;
}
