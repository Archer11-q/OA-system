package com.oasystem.attendance.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class LeaveRequestDTO {
    @NotNull
    private Integer leaveType;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    private Float days;

    private String reason;
}

