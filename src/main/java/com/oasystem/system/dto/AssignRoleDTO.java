package com.oasystem.system.dto;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class AssignRoleDTO {
    @NotEmpty
    private List<Long> roleIds;

    /** 模式: replace（覆盖）或 append（追加），默认 replace */
    private String mode = "replace";
}

