package com.oasystem.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@Schema(description = "请假申请请求参数")
public class LeaveRequestDTO {
    @NotNull
    @Schema(description = "请假类型（1=事假，2=病假，3=年假，4=婚假，5=其他）", example = "1")
    private Integer leaveType;

    @NotNull
    @Schema(description = "开始日期", example = "2026-06-15")
    private LocalDate startDate;

    @NotNull
    @Schema(description = "结束日期", example = "2026-06-15")
    private LocalDate endDate;

    @Schema(description = "请假天数", example = "1")
    private Float days;

    @Schema(description = "请假原因", example = "个人事务")
    private String reason;
}

