package com.oasystem.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oasystem.common.exception.BusinessException;
import com.oasystem.security.JwtTokenProvider;
import com.oasystem.system.dto.LoginDTO;
import com.oasystem.system.entity.User;
import com.oasystem.system.mapper.RoleMapper;
import com.oasystem.system.mapper.UserMapper;
import com.oasystem.system.mapper.UserRoleMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 用户管理 Service 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户管理 Service 单元测试")
class UserServiceImplTest {

    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private UserRoleMapper userRoleMapper;
    @Mock private RoleMapper roleMapper;
    @Mock private com.oasystem.security.SecurityUtils securityUtils;

    @InjectMocks
    private UserServiceImpl userService;

    // ==================== 登录测试 ====================

    @Test
    @DisplayName("登录 — 成功返回Token+用户信息")
    void testLogin_Success() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("123456");

        User user = buildUser(1L, "admin", "encoded_pwd", 1);

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
        when(passwordEncoder.matches("123456", "encoded_pwd")).thenReturn(true);
        when(jwtTokenProvider.generateToken(eq(1L), eq("admin"), anyMap())).thenReturn("jwt-token-xxx");

        Map<String, Object> result = userService.login(dto);

        assertNotNull(result);
        assertEquals("jwt-token-xxx", result.get("token"));
        assertEquals(1L, result.get("userId"));
        assertEquals("admin", result.get("username"));
    }

    @Test
    @DisplayName("登录 — 密码错误")
    void testLogin_BadCredentials() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("wrong_password");

        User user = buildUser(1L, "admin", "encoded_pwd", 1);

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
        when(passwordEncoder.matches("wrong_password", "encoded_pwd")).thenReturn(false);

        assertThrows(BusinessException.class, () -> userService.login(dto));
    }

    @Test
    @DisplayName("登录 — 用户不存在")
    void testLogin_UserNotFound() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("nonexistent");
        dto.setPassword("123456");

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        assertThrows(BusinessException.class, () -> userService.login(dto));
    }

    @Test
    @DisplayName("登录 — 账号被禁用")
    void testLogin_DisabledUser() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("disabled_user");
        dto.setPassword("123456");

        User user = buildUser(2L, "disabled_user", "encoded_pwd", 0); // status=0 禁用

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);

        assertThrows(BusinessException.class, () -> userService.login(dto));
    }

    // ==================== 新增用户 ====================

    @Test
    @DisplayName("新增用户 — 成功")
    void testAddUser_Success() {
        User newUser = buildUser(null, "newuser", "123456", 1);

        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(passwordEncoder.encode("123456")).thenReturn("bcrypt_encoded");
        when(userMapper.insert(any(User.class))).thenReturn(1);

        userService.addUser(newUser);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(captor.capture());
        User saved = captor.getValue();
        assertEquals("newuser", saved.getUsername());
        assertEquals("bcrypt_encoded", saved.getPassword());
        assertEquals(1, saved.getStatus());
    }

    @Test
    @DisplayName("新增用户 — 用户名已存在")
    void testAddUser_DuplicateUsername() {
        User newUser = buildUser(null, "admin", "123456", 1);

        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        assertThrows(BusinessException.class, () -> userService.addUser(newUser));
        verify(userMapper, never()).insert(any());
    }

    // ==================== 重置密码 ====================

    @Test
    @DisplayName("重置密码 — 成功重置为123456")
    void testResetPassword_Success() {
        User user = buildUser(1L, "admin", "old_pwd", 1);

        when(userMapper.selectById(1L)).thenReturn(user);
        when(passwordEncoder.encode("123456")).thenReturn("new_bcrypt");
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        userService.resetPassword(1L);

        assertEquals("new_bcrypt", user.getPassword());
        verify(userMapper).updateById(user);
    }

    @Test
    @DisplayName("重置密码 — 用户不存在")
    void testResetPassword_UserNotFound() {
        when(userMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> userService.resetPassword(999L));
        verify(userMapper, never()).updateById(any());
    }

    // ==================== 辅助方法 ====================

    private User buildUser(Long id, String username, String password, int status) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(password);
        user.setRealName("测试用户");
        user.setStatus(status);
        user.setDeptId(2L);
        return user;
    }
}
