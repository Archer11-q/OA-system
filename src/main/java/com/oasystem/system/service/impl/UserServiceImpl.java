package com.oasystem.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oasystem.common.PageResult;
import com.oasystem.common.exception.BusinessException;
import com.oasystem.security.JwtTokenProvider;
import com.oasystem.system.dto.LoginDTO;
import com.oasystem.system.dto.UserQueryDTO;
import com.oasystem.system.entity.User;
import com.oasystem.system.mapper.UserMapper;
import com.oasystem.system.service.UserService;
import com.oasystem.system.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final com.oasystem.system.mapper.UserRoleMapper userRoleMapper;
    private final com.oasystem.system.mapper.RoleMapper roleMapper;
    private final com.oasystem.system.mapper.MenuMapper menuMapper;
    private final com.oasystem.security.SecurityUtils securityUtils;

    @Override
    public Map<String, Object> login(LoginDTO loginDTO) {
        // 查询用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, loginDTO.getUsername())
        );
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        // 验证状态
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }
        // 验证密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 生成 Token
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), new HashMap<>());

        // 组装返回
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        return result;
    }

    @Override
    public PageResult<UserVO> pageQuery(UserQueryDTO query) {
        // 注入 dataScope 过滤
        try {
            Long currentUserId = securityUtils.getCurrentUserId();
            if (currentUserId != null) {
                java.util.List<com.oasystem.system.entity.Role> roles = this.getUserRoles(currentUserId);
                // 找到最宽松的 dataScope（数值越小越宽松，1=全部）
                int minScope = roles.stream().mapToInt(r -> r.getDataScope() == null ? 4 : r.getDataScope()).min().orElse(4);
                if (minScope == 1) {
                    // 全部数据，no-op
                } else if (minScope == 4) {
                    // 仅本人
                    String username = securityUtils.getCurrentUsername();
                    query.setOnlyUsername(username);
                } else {
                    // 本部门或本部门及子部门，简化实现：使用当前用户的 deptId
                    User me = userMapper.selectById(currentUserId);
                    if (me != null && me.getDeptId() != null) {
                        query.setAllowedDeptIds(java.util.Collections.singletonList(me.getDeptId()));
                    }
                }
            }
        } catch (Exception ex) {
            // ignore
        }
        Page<User> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<User> result = userMapper.selectUserPage(page, query);

        return new PageResult<>(
                result.getCurrent(),
                result.getSize(),
                result.getTotal(),
                result.getRecords().stream().map(this::toVO).collect(Collectors.toList())
        );
    }

    @Override
    @Transactional
    public void addUser(User user) {
        // 校验用户名唯一
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, user.getUsername())
        );
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(1);
        userMapper.insert(user);
        log.info("用户创建成功: username={}", user.getUsername());
    }

    @Override
    @Transactional
    public void updateUser(User user) {
        // 修改密码时需加密
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null);  // 不更新密码字段
        }
        userMapper.updateById(user);
        log.info("用户更新成功: userId={}", user.getId());
    }

    @Override
    @Transactional
    public void resetPassword(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(passwordEncoder.encode("123456"));
        userMapper.updateById(user);
        log.info("密码已重置: userId={}", userId);
    }

    @Override
    public UserVO getCurrentUserInfo() {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            // 开发环境兜底：无认证上下文时默认返回admin
            userId = 1L;
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        UserVO vo = toVO(user);

        // 填充角色和权限信息（供前端 store 使用）
        java.util.List<com.oasystem.system.entity.Role> roles = getUserRoles(userId);
        if (roles != null && !roles.isEmpty()) {
            java.util.List<String> roleCodes = roles.stream()
                    .map(com.oasystem.system.entity.Role::getRoleCode)
                    .filter(r -> r != null)
                    .collect(java.util.stream.Collectors.toList());
            java.util.List<String> roleNames = roles.stream()
                    .map(com.oasystem.system.entity.Role::getRoleName)
                    .filter(r -> r != null)
                    .collect(java.util.stream.Collectors.toList());
            vo.setRoleNames(roleNames);
            vo.setRoles(roleCodes);
        }

        // 填充菜单权限标识
        java.util.List<com.oasystem.system.entity.Menu> menus =
                menuMapper.selectMenusByUserId(userId);
        if (menus != null && !menus.isEmpty()) {
            java.util.List<String> perms = menus.stream()
                    .map(com.oasystem.system.entity.Menu::getPerms)
                    .filter(p -> p != null && !p.isEmpty())
                    .distinct()
                    .collect(java.util.stream.Collectors.toList());
            vo.setPermissions(perms);
        }

        return vo;
    }

    @Override
    public java.util.List<com.oasystem.system.entity.Role> getUserRoles(Long userId) {
        return roleMapper.selectRolesByUserId(userId);
    }

    @Override
    @Transactional
    public void assignRolesToUser(Long userId, java.util.List<Long> roleIds) {
        // 覆盖式分配：先删除原有，再批量插入
        userRoleMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.oasystem.system.entity.UserRole>()
                .eq(com.oasystem.system.entity.UserRole::getUserId, userId));
        if (roleIds == null || roleIds.isEmpty()) return;
        for (Long rid : roleIds) {
            com.oasystem.system.entity.UserRole ur = new com.oasystem.system.entity.UserRole();
            ur.setUserId(userId);
            ur.setRoleId(rid);
            userRoleMapper.insert(ur);
        }
    }

    /**
     * Entity → VO 转换
     */
    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
