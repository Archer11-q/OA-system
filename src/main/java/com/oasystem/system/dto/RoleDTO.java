package com.oasystem.system.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 角色 DTO（新增/更新）
 */
@Data
public class RoleDTO {

    private Long id;

    @NotBlank
    private String roleName;

    @NotBlank
    private String roleCode;

    private String description;

    private Integer sort = 0;

    @NotNull
    private Integer status = 1;

    private Integer dataScope = 1;
}

