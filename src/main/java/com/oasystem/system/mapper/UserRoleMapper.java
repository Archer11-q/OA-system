package com.oasystem.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oasystem.system.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户-角色 关联 Mapper
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
}

