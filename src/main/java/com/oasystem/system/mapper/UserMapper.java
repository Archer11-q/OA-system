package com.oasystem.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oasystem.system.entity.User;
import com.oasystem.system.dto.UserQueryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户 Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 分页查询用户（含部门名称）
     */
    IPage<User> selectUserPage(Page<User> page, @Param("query") UserQueryDTO query);

    /**
     * 根据用户名查询用户（含角色信息）
     */
    User selectByUsername(@Param("username") String username);
}
