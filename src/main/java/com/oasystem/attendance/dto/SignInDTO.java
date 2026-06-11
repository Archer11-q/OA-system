package com.oasystem.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
@Schema(description = "签到/签退请求参数")
public class SignInDTO {
    @NotBlank
    @Schema(description = "打卡位置", example = "公司大楼A座")
    private String location;
}

