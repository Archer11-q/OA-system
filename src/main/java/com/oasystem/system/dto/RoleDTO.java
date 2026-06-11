package com.oasystem.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 角色 DTO（新增/更新）
 */
@Data
@Schema(description = "角色新增/更新请求参数")
public class RoleDTO {

    @Schema(description = "角色ID（更新时必填）")
    private Long id;

    @NotBlank
    @Schema(description = "角色名称", example = "部门经理")
    private String roleName;

    @NotBlank
    @Schema(description = "角色编码（唯一）", example = "ROLE_MANAGER")
    private String roleCode;

    @Schema(description = "角色描述")
    private String description;

    @Schema(description = "排序号", example = "0")
    private Integer sort = 0;

    @NotNull
    @Schema(description = "状态（0=禁用，1=启用）", example = "1")
    private Integer status = 1;

    @Schema(description = "数据权限范围（1=全部，2=本部门，3=本部门及子部门，4=仅本人）", example = "1")
    private Integer dataScope = 1;
}

