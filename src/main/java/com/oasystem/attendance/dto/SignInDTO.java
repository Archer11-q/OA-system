package com.oasystem.attendance.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class SignInDTO {
    @NotBlank
    private String location;
}

