package com.oasystem.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Data
@Schema(description = "角色分配请求参数")
public class AssignRoleDTO {
    @NotEmpty
    @Schema(description = "角色ID列表", example = "[1, 2, 3]")
    private List<Long> roleIds;

    @Schema(description = "分配模式: replace（覆盖）或 append（追加），默认 replace", example = "replace")
    private String mode = "replace";
}

