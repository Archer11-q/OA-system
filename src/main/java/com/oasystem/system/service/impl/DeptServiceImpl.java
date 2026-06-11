package com.oasystem.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oasystem.system.entity.Dept;
import com.oasystem.system.entity.User;
import com.oasystem.system.mapper.DeptMapper;
import com.oasystem.system.mapper.UserMapper;
import com.oasystem.system.service.DeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeptServiceImpl extends ServiceImpl<DeptMapper, Dept> implements DeptService {

    private final DeptMapper deptMapper;
    private final UserMapper userMapper;

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

    @Override
    public List<Map<String, Object>> getUserStats() {
        List<Dept> allDepts = listAll();
        List<User> allUsers = userMapper.selectList(
            new LambdaQueryWrapper<User>().eq(User::getStatus, 1)
        );

        if (allDepts.isEmpty()) {
            return Collections.emptyList();
        }

        // 按部门统计直接用户数
        Map<Long, Long> directCountMap = allUsers.stream()
            .filter(u -> u.getDeptId() != null)
            .collect(Collectors.groupingBy(User::getDeptId, Collectors.counting()));

        // 构建部门ID到子部门ID集合的映射（用于计算含子部门的总数）
        Map<Long, List<Long>> subDeptMap = new HashMap<>();
        Map<Long, Dept> deptMap = allDepts.stream().collect(Collectors.toMap(Dept::getId, d -> d));

        for (Dept dept : allDepts) {
            List<Long> subIds = allDepts.stream()
                .filter(d -> d.getParentId() != null && d.getParentId().equals(dept.getId()))
                .map(Dept::getId)
                .collect(Collectors.toList());
            // 递归收集所有子部门ID
            List<Long> allSubIds = new ArrayList<>(subIds);
            for (Long subId : subIds) {
                collectSubDeptIds(subId, deptMap, allSubIds);
            }
            subDeptMap.put(dept.getId(), allSubIds);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Dept dept : allDepts) {
            Map<String, Object> stat = new LinkedHashMap<>();
            stat.put("deptId", dept.getId());
            stat.put("deptName", dept.getDeptName());
            stat.put("parentId", dept.getParentId() != null ? dept.getParentId() : 0L);

            // 直接归属该部门的用户数
            long directCount = directCountMap.getOrDefault(dept.getId(), 0L);
            stat.put("directCount", directCount);

            // 含子部门的总用户数
            long totalCount = directCount;
            for (Long subId : subDeptMap.getOrDefault(dept.getId(), Collections.emptyList())) {
                totalCount += directCountMap.getOrDefault(subId, 0L);
            }
            stat.put("totalCount", totalCount);

            result.add(stat);
        }
        return result;
    }

    /**
     * 递归收集子部门ID
     */
    private void collectSubDeptIds(Long deptId, Map<Long, Dept> deptMap, List<Long> collected) {
        List<Long> children = deptMap.values().stream()
            .filter(d -> d.getParentId() != null && d.getParentId().equals(deptId))
            .map(Dept::getId)
            .collect(Collectors.toList());
        for (Long childId : children) {
            if (!collected.contains(childId)) {
                collected.add(childId);
                collectSubDeptIds(childId, deptMap, collected);
            }
        }
    }
}

