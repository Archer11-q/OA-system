package com.oasystem.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "角色信息视图对象")
public class RoleVO {
    @Schema(description = "角色ID")
    private Long id;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "角色编码")
    private String roleCode;

    @Schema(description = "角色描述")
    private String description;

    @Schema(description = "排序号")
    private Integer sort;

    @Schema(description = "状态（0=禁用，1=启用）")
    private Integer status;

    @Schema(description = "数据权限范围（1=全部，2=本部门，3=本部门及子部门，4=仅本人）")
    private Integer dataScope;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "已分配的菜单ID列表")
    private java.util.List<Long> menuIds;
}

