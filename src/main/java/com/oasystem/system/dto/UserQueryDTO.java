package com.oasystem.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户查询DTO
 */
@Data
@Schema(description = "用户分页查询条件")
public class UserQueryDTO {

    @Schema(description = "当前页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页条数", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "用户名（模糊查询）")
    private String username;

    @Schema(description = "真实姓名（模糊查询）")
    private String realName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "所属部门ID")
    private Long deptId;

    @Schema(description = "允许查询的部门ID集合（由 dataScope 决定，后端注入）", hidden = true)
    private java.util.List<Long> allowedDeptIds;

    @Schema(description = "仅按用户名精确过滤（dataScope=4时使用）", hidden = true)
    private String onlyUsername;

    @Schema(description = "状态（0=禁用，1=启用）")
    private Integer status;
}
