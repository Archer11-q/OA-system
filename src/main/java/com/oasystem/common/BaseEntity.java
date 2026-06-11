package com.oasystem.common;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体基类
 * <p>
 * 所有数据库实体继承此类，统一管理通用字段。
 */
@Data
public abstract class BaseEntity implements Serializable {

    @Schema(description = "主键ID（自增）")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "创建时间（自动填充）")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间（自动填充）")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "创建人ID")
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    @Schema(description = "更新人ID")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    @Schema(description = "逻辑删除标记（0=正常，1=已删除）", hidden = true)
    @TableLogic
    private Integer deleted;

    @Schema(description = "备注")
    private String remark;
}
