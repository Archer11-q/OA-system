package com.oasystem.log.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体
 */
@Data
@TableName("sys_oper_log")
public class OperLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作人ID */
    private Long userId;

    /** 操作人用户名 */
    private String username;

    /** 操作模块 */
    private String module;

    /** 操作描述 */
    private String operation;

    /** 请求方法（类名.方法名） */
    private String method;

    /** 请求方式（GET/POST/PUT/DELETE） */
    private String requestMethod;

    /** 请求URL */
    private String url;

    /** 操作IP */
    private String ip;

    /** 请求参数（JSON） */
    private String requestParams;

    /** 返回结果（JSON，截断） */
    private String result;

    /** 耗时（毫秒） */
    private Long costTime;

    /** 状态（0=失败，1=成功） */
    private Integer status;

    /** 错误信息 */
    private String errorMsg;

    /** 创建时间 */
    private LocalDateTime createTime;
}
