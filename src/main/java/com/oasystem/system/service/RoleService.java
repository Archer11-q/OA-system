package com.oasystem.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oasystem.system.entity.Role;

import java.util.List;

/**
 * 角色服务接口
 */
public interface RoleService extends IService<Role> {

    List<Role> listAll();

    Role getByIdWithCheck(Long id);

    void createRole(Role role);

    void updateRole(Role role);

    void deleteRole(Long id);

    void assignMenus(Long roleId, List<Long> menuIds);

    /**
     * 获取角色已分配的菜单ID列表
     */
    List<Long> getMenuIdsByRoleId(Long roleId);
}

