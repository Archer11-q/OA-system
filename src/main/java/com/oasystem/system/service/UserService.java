package com.oasystem.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oasystem.common.PageResult;
import com.oasystem.system.dto.LoginDTO;
import com.oasystem.system.dto.UserQueryDTO;
import com.oasystem.system.entity.User;
import com.oasystem.system.vo.UserVO;

import java.util.Map;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return Token 及用户信息
     */
    Map<String, Object> login(LoginDTO loginDTO);

    /**
     * 分页查询用户
     */
    PageResult<UserVO> pageQuery(UserQueryDTO query);

    /**
     * 新增用户
     */
    void addUser(User user);

    /**
     * 更新用户
     */
    void updateUser(User user);

    /**
     * 重置密码
     */
    void resetPassword(Long userId);

    /**
     * 获取当前登录用户信息
     */
    UserVO getCurrentUserInfo();

    /**
     * 查询用户已分配的角色
     */
    java.util.List<com.oasystem.system.entity.Role> getUserRoles(Long userId);

    /**
     * 为用户分配角色（覆盖式）
     */
    void assignRolesToUser(Long userId, java.util.List<Long> roleIds);
}
