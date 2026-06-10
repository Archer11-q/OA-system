package com.oasystem.expense.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oasystem.common.constant.Constants;
import com.oasystem.common.exception.BusinessException;
import com.oasystem.expense.entity.ExpenseRequest;
import com.oasystem.expense.mapper.ExpenseMapper;
import com.oasystem.expense.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * 报销管理 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseMapper expenseMapper;

    @Override
    public List<ExpenseRequest> listByUser(Long userId, Integer status) {
        LambdaQueryWrapper<ExpenseRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExpenseRequest::getUserId, userId);
        if (status != null) {
            wrapper.eq(ExpenseRequest::getStatus, status);
        }
        wrapper.orderByDesc(ExpenseRequest::getCreateTime);
        return expenseMapper.selectList(wrapper);
    }

    @Override
    public ExpenseRequest getById(Long id) {
        return expenseMapper.selectById(id);
    }

    @Override
    public void create(ExpenseRequest expense) {
        if (expense.getTitle() == null || expense.getTitle().isBlank()) {
            throw new BusinessException("报销标题不能为空");
        }
        if (expense.getAmount() == null || expense.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("报销金额必须大于0");
        }
        if (expense.getExpenseType() == null) {
            throw new BusinessException("报销类型不能为空");
        }
        expense.setStatus(Constants.APPROVAL_PENDING);
        expenseMapper.insert(expense);
        log.info("报销申请提交成功, id={}, amount={}", expense.getId(), expense.getAmount());
    }

    @Override
    public void update(ExpenseRequest expense, Long userId) {
        ExpenseRequest exist = expenseMapper.selectById(expense.getId());
        if (exist == null) {
            throw new BusinessException("报销申请不存在");
        }
        if (!exist.getUserId().equals(userId)) {
            throw new BusinessException("只能修改自己的报销申请");
        }
        if (!exist.getStatus().equals(Constants.APPROVAL_PENDING)) {
            throw new BusinessException("只能修改待审批状态的报销申请");
        }
        if (expense.getAmount() != null && expense.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("报销金额必须大于0");
        }
        // 不允许通过此接口修改状态和审批相关字段
        expense.setStatus(null);
        expense.setApproverId(null);
        expense.setApprovalComment(null);
        expense.setApprovalTime(null);
        expenseMapper.updateById(expense);
        log.info("报销申请更新成功, id={}", expense.getId());
    }

    @Override
    public void delete(Long id, Long userId) {
        ExpenseRequest exist = expenseMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException("报销申请不存在");
        }
        if (!exist.getUserId().equals(userId)) {
            throw new BusinessException("只能删除自己的报销申请");
        }
        if (!exist.getStatus().equals(Constants.APPROVAL_PENDING)) {
            throw new BusinessException("只能删除待审批状态的报销申请");
        }
        expenseMapper.deleteById(id);
        log.info("报销申请删除成功, id={}", id);
    }

    @Override
    public Map<String, Object> stats(Long userId) {
        Map<String, Object> result = new LinkedHashMap<>();

        // 总申请数
        Long totalCount = expenseMapper.selectCount(
                new LambdaQueryWrapper<ExpenseRequest>().eq(ExpenseRequest::getUserId, userId));
        result.put("totalCount", totalCount);

        // 按状态统计数量和金额
        List<ExpenseRequest> all = expenseMapper.selectList(
                new LambdaQueryWrapper<ExpenseRequest>().eq(ExpenseRequest::getUserId, userId));

        long pendingCount = 0, approvedCount = 0, rejectedCount = 0;
        BigDecimal pendingAmount = BigDecimal.ZERO;
        BigDecimal approvedAmount = BigDecimal.ZERO;
        BigDecimal rejectedAmount = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (ExpenseRequest e : all) {
            BigDecimal amt = e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO;
            totalAmount = totalAmount.add(amt);
            if (e.getStatus().equals(Constants.APPROVAL_PENDING)) {
                pendingCount++;
                pendingAmount = pendingAmount.add(amt);
            } else if (e.getStatus().equals(Constants.APPROVAL_APPROVED)) {
                approvedCount++;
                approvedAmount = approvedAmount.add(amt);
            } else if (e.getStatus().equals(Constants.APPROVAL_REJECTED)) {
                rejectedCount++;
                rejectedAmount = rejectedAmount.add(amt);
            }
        }

        result.put("totalAmount", totalAmount);
        result.put("pendingCount", pendingCount);
        result.put("pendingAmount", pendingAmount);
        result.put("approvedCount", approvedCount);
        result.put("approvedAmount", approvedAmount);
        result.put("rejectedCount", rejectedCount);
        result.put("rejectedAmount", rejectedAmount);

        return result;
    }
}
