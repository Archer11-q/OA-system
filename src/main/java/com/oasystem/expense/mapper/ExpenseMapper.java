package com.oasystem.expense.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oasystem.expense.entity.ExpenseRequest;
import org.apache.ibatis.annotations.Mapper;

/**
 * 报销申请 Mapper
 */
@Mapper
public interface ExpenseMapper extends BaseMapper<ExpenseRequest> {
}
