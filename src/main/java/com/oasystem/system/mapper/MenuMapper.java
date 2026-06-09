package com.oasystem.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oasystem.system.entity.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜单 Mapper
 */
@Mapper
public interface MenuMapper extends BaseMapper<Menu> {

    /**
     * 查询用户拥有的菜单权限
     */
    List<Menu> selectMenusByUserId(@Param("userId") Long userId);
}
