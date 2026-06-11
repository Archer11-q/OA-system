package com.oasystem.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oasystem.system.entity.Role;
import com.oasystem.system.entity.RoleMenu;
import com.oasystem.system.mapper.RoleMapper;
import com.oasystem.system.mapper.RoleMenuMapper;
import com.oasystem.system.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private final RoleMapper roleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final com.oasystem.system.mapper.UserRoleMapper userRoleMapper;

    @Override
    public List<Role> listAll() {
        return roleMapper.selectList(null);
    }

    @Override
    public Role getByIdWithCheck(Long id) {
        return roleMapper.selectById(id);
    }

    @Override
    public void createRole(Role role) {
        // roleCode 唯一校验
        if (role.getRoleCode() != null) {
            Role exist = roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getRoleCode, role.getRoleCode()));
            if (exist != null) {
                throw new com.oasystem.common.exception.BusinessException("角色编码已存在");
            }
        }
        roleMapper.insert(role);
    }

    @Override
    public void updateRole(Role role) {
        // 唯一校验 roleCode（排除自身）
        if (role.getRoleCode() != null) {
            Role exist = roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getRoleCode, role.getRoleCode()).ne(Role::getId, role.getId()));
            if (exist != null) {
                throw new com.oasystem.common.exception.BusinessException("角色编码已存在");
            }
        }
        roleMapper.updateById(role);
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        // 检查是否有用户关联该角色，若有则禁止删除
        long count = userRoleMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.oasystem.system.entity.UserRole>().eq(com.oasystem.system.entity.UserRole::getRoleId, id));
        if (count > 0) {
            throw new com.oasystem.common.exception.BusinessException("角色已被分配给用户，无法删除");
        }
        // 删除角色
        roleMapper.deleteById(id);
        // 删除关联的角色-菜单
        roleMenuMapper.delete(new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, id));
    }

    @Override
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        List<RoleMenu> list = roleMenuMapper.selectList(
                new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, roleId));
        return list.stream().map(RoleMenu::getMenuId).toList();
    }

    @Override
    @Transactional
    public void assignMenus(Long roleId, List<Long> menuIds) {
        // 先删除原有关联
        roleMenuMapper.delete(new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, roleId));
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }
        List<RoleMenu> list = new ArrayList<>();
        for (Long mid : menuIds) {
            if (Objects.isNull(mid)) continue;
            RoleMenu rm = new RoleMenu();
            rm.setRoleId(roleId);
            rm.setMenuId(mid);
            list.add(rm);
        }
        // 批量插入
        for (RoleMenu rm : list) {
            roleMenuMapper.insert(rm);
        }
    }
}

