package com.oasystem.approval.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
@Schema(description = "审批操作请求参数")
public class ApproveDTO {
    @NotNull
    @Schema(description = "审批结果（1=同意，2=驳回）", example = "1")
    private Integer result;

    @Schema(description = "审批意见", example = "同意请假")
    private String comment;
}

