package com.oasystem.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oasystem.system.entity.Dept;

import java.util.List;
import java.util.Map;

public interface DeptService extends IService<Dept> {
    List<Dept> listAll();

    Dept getByIdWithCheck(Long id);

    void createDept(Dept dept);

    void updateDept(Dept dept);

    void deleteDept(Long id);

    /**
     * 获取各部门用户统计（含子部门汇总）
     * @return 每个部门的统计数据：[{deptId, deptName, directCount, totalCount}, ...]
     */
    List<Map<String, Object>> getUserStats();
}

