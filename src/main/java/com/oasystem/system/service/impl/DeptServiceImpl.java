package com.oasystem.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oasystem.system.entity.Dept;
import com.oasystem.system.mapper.DeptMapper;
import com.oasystem.system.service.DeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeptServiceImpl extends ServiceImpl<DeptMapper, Dept> implements DeptService {

    private final DeptMapper deptMapper;

    @Override
    public List<Dept> listAll() {
        return deptMapper.selectList(null);
    }

    @Override
    public Dept getByIdWithCheck(Long id) {
        return deptMapper.selectById(id);
    }

    @Override
    public void createDept(Dept dept) {
        // 计算 ancestors
        if (dept.getParentId() == null || dept.getParentId() == 0L) {
            dept.setAncestors("0");
        } else {
            Dept parent = deptMapper.selectById(dept.getParentId());
            String pa = parent != null ? parent.getAncestors() + "," + parent.getId() : "0";
            dept.setAncestors(pa);
        }
        deptMapper.insert(dept);
    }

    @Override
    public void updateDept(Dept dept) {
        deptMapper.updateById(dept);
    }

    @Override
    @Transactional
    public void deleteDept(Long id) {
        // 简单删除，不级联
        deptMapper.deleteById(id);
    }
}

