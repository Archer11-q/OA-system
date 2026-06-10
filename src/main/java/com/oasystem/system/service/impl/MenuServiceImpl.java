package com.oasystem.system.service.impl;

import com.oasystem.system.entity.Menu;
import com.oasystem.system.mapper.MenuMapper;
import com.oasystem.system.mapper.RoleMenuMapper;
import com.oasystem.system.service.MenuService;
import com.oasystem.system.vo.MenuTreeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜单业务实现
 */
@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {
    private final MenuMapper menuMapper;
    private final RoleMenuMapper roleMenuMapper;

    @Override
    public List<MenuTreeVO> tree(boolean all, Long userId) {
        List<Menu> menus;
        if (all) {
            menus = menuMapper.selectList(null);
        } else {
            menus = menuMapper.selectMenusByUserId(userId);
        }
        List<MenuTreeVO> vos = menus.stream().map(m -> {
            MenuTreeVO vo = new MenuTreeVO();
            vo.setId(m.getId());
            vo.setParentId(m.getParentId());
            vo.setMenuName(m.getMenuName());
            vo.setPath(m.getPath());
            vo.setComponent(m.getComponent());
            vo.setIcon(m.getIcon());
            vo.setMenuType(m.getMenuType());
            vo.setSort(m.getSort());
            vo.setVisible(m.getVisible());
            return vo;
        }).collect(Collectors.toList());

        Map<Long, MenuTreeVO> map = vos.stream().collect(Collectors.toMap(MenuTreeVO::getId, v -> v));
        List<MenuTreeVO> roots = vos.stream().filter(v -> v.getParentId() == null || v.getParentId() == 0L).collect(Collectors.toList());
        for (MenuTreeVO vo : vos) {
            if (vo.getParentId() != null && vo.getParentId() != 0L) {
                MenuTreeVO parent = map.get(vo.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) parent.setChildren(new java.util.ArrayList<>());
                    parent.getChildren().add(vo);
                }
            }
        }
        return roots;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(Menu menu) {
        if (menu == null) throw new IllegalArgumentException("参数为空");
        if (menu.getPerms() != null && !menu.getPerms().isEmpty()) {
            long cnt = menuMapper.selectCount(new LambdaQueryWrapper<Menu>().eq(Menu::getPerms, menu.getPerms()));
            if (cnt > 0) throw new IllegalStateException("权限标识已存在");
        }
        menuMapper.insert(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Menu menu) {
        if (menu == null || menu.getId() == null) throw new IllegalArgumentException("参数错误");
        if (menu.getPerms() != null && !menu.getPerms().isEmpty()) {
            long cnt = menuMapper.selectCount(new LambdaQueryWrapper<Menu>().eq(Menu::getPerms, menu.getPerms()).ne(Menu::getId, menu.getId()));
            if (cnt > 0) throw new IllegalStateException("权限标识已存在");
        }
        menuMapper.updateById(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (id == null) throw new IllegalArgumentException("参数为空");
        long refCount = roleMenuMapper.selectCount(new LambdaQueryWrapper<com.oasystem.system.entity.RoleMenu>().eq(com.oasystem.system.entity.RoleMenu::getMenuId, id));
        if (refCount > 0) {
            throw new IllegalStateException("该菜单已被角色引用，不能删除，请先从角色中移除引用");
        }
        menuMapper.deleteById(id);
    }
}

