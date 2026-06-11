package com.oasystem.approval.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发起审批请求 DTO
 */
@Data
@Schema(description = "发起审批请求参数")
public class StartApprovalDTO {

    @NotNull(message = "审批模板不能为空")
    @Schema(description = "审批模板ID", example = "1")
    private Long templateId;

    @NotBlank(message = "审批标题不能为空")
    @Schema(description = "审批标题", example = "请假申请")
    private String title;

    @Schema(description = "审批内容（JSON格式存储表单数据）")
    private String content;

    @Schema(description = "关联业务类型（LEAVE=请假，EXPENSE=报销）", example = "LEAVE")
    private String businessType;

    @Schema(description = "关联业务ID")
    private Long businessId;
}
