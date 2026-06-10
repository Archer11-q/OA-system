package com.oasystem.expense.service;

import com.oasystem.expense.entity.ExpenseRequest;

import java.util.List;
import java.util.Map;

/**
 * 报销管理 Service 接口
 */
public interface ExpenseService {

    /**
     * 查询报销列表（按用户过滤，可选状态）
     */
    List<ExpenseRequest> listByUser(Long userId, Integer status);

    /**
     * 根据ID查询报销详情
     */
    ExpenseRequest getById(Long id);

    /**
     * 提交报销申请
     */
    void create(ExpenseRequest expense);

    /**
     * 更新报销申请（仅待审批状态可修改）
     */
    void update(ExpenseRequest expense, Long userId);

    /**
     * 删除报销申请（仅待审批状态可删除）
     */
    void delete(Long id, Long userId);

    /**
     * 报销统计（按状态汇总金额）
     */
    Map<String, Object> stats(Long userId);
}
