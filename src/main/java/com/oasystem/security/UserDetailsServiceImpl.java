package com.oasystem.security;

import com.oasystem.system.entity.Menu;
import com.oasystem.system.entity.Role;
import com.oasystem.system.entity.User;
import com.oasystem.system.mapper.MenuMapper;
import com.oasystem.system.mapper.RoleMapper;
import com.oasystem.system.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final MenuMapper menuMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
        );
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        Long userId = user.getId();
        // 角色权限
        List<Role> roles = roleMapper.selectRolesByUserId(userId);
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        if (roles != null) {
            for (Role r : roles) {
                if (r.getRoleCode() != null) {
                    authorities.add(new SimpleGrantedAuthority(r.getRoleCode()));
                }
            }
        }

        // 菜单权限 perms
        List<Menu> menus = menuMapper.selectMenusByUserId(userId);
        if (menus != null) {
            for (Menu m : menus) {
                if (m.getPerms() != null && !m.getPerms().isEmpty()) {
                    // 支持逗号分隔的 perms
                    String[] parts = m.getPerms().split(",");
                    for (String p : parts) {
                        String perm = p.trim();
                        if (!perm.isEmpty()) authorities.add(new SimpleGrantedAuthority(perm));
                    }
                }
            }
        }

        return new JwtUserDetails(userId, user.getUsername(), authorities);
    }
}

