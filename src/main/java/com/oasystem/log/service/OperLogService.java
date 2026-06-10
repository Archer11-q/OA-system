package com.oasystem.log.service;

import com.oasystem.common.PageResult;
import com.oasystem.log.entity.OperLog;

/**
 * 操作日志 Service 接口
 */
public interface OperLogService {

    /**
     * 分页查询操作日志
     */
    PageResult<OperLog> page(int pageNum, int pageSize, String username, String module, Integer status);

    /**
     * 根据ID查询日志详情
     */
    OperLog getById(Long id);

    /**
     * 清理N天前的日志
     */
    int cleanOldLogs(int days);
}
