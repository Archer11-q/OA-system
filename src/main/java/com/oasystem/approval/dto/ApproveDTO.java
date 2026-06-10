package com.oasystem.approval.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
public class ApproveDTO {
    @NotNull
    private Integer result; // 1=同意,2=驳回

    private String comment;
}

