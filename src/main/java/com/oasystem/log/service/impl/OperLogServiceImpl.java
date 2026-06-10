package com.oasystem.log.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oasystem.common.PageResult;
import com.oasystem.common.exception.BusinessException;
import com.oasystem.log.entity.OperLog;
import com.oasystem.log.mapper.OperLogMapper;
import com.oasystem.log.service.OperLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 操作日志 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperLogServiceImpl implements OperLogService {

    private final OperLogMapper operLogMapper;

    @Override
    public PageResult<OperLog> page(int pageNum, int pageSize, String username, String module, Integer status) {
        LambdaQueryWrapper<OperLog> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isBlank()) {
            wrapper.like(OperLog::getUsername, username);
        }
        if (module != null && !module.isBlank()) {
            wrapper.eq(OperLog::getModule, module);
        }
        if (status != null) {
            wrapper.eq(OperLog::getStatus, status);
        }
        wrapper.orderByDesc(OperLog::getCreateTime);

        IPage<OperLog> iPage = operLogMapper.selectPage(
                new Page<>(pageNum, pageSize), wrapper);

        return new PageResult<>(
                iPage.getCurrent(),
                iPage.getSize(),
                iPage.getTotal(),
                iPage.getRecords()
        );
    }

    @Override
    public OperLog getById(Long id) {
        OperLog log = operLogMapper.selectById(id);
        if (log == null) {
            throw new BusinessException("日志记录不存在");
        }
        return log;
    }

    @Override
    public int cleanOldLogs(int days) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        LambdaQueryWrapper<OperLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(OperLog::getCreateTime, cutoff);
        int count = operLogMapper.delete(wrapper);
        log.info("清理了 {} 条 {} 天前的操作日志", count, days);
        return count;
    }
}
