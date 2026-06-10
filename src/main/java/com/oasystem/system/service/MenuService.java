package com.oasystem.system.service;

import com.oasystem.system.entity.Menu;
import com.oasystem.system.vo.MenuTreeVO;
import java.util.List;

/**
 * 菜单业务接口
 */
public interface MenuService {
    List<MenuTreeVO> tree(boolean all, Long userId);

    void add(Menu menu);

    void update(Menu menu);

    void delete(Long id);
}

