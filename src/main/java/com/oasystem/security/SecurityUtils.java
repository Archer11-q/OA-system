package com.oasystem.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 安全工具类
 * <p>
 * 提供获取当前登录用户信息的便捷方法。
 *
 * TODO: 完成 Spring Security 集成后，从 SecurityContextHolder 获取当前用户。
 */
@Slf4j
@Component
public class SecurityUtils {

    /**
     * 获取当前登录用户ID
     * TODO: 从 SecurityContextHolder 获取
     */
    public Long getCurrentUserId() {
        try {
            Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof JwtUserDetails) {
                return ((JwtUserDetails) principal).getId();
            }
            if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                // 无 id 信息时返回 null
                return null;
            }
        } catch (Exception ex) {
            log.debug("getCurrentUserId error", ex);
        }
        return null;
    }

    /**
     * 获取当前登录用户名
     * TODO: 从 SecurityContextHolder 获取
     */
    public String getCurrentUsername() {
        try {
            Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof JwtUserDetails) {
                return ((JwtUserDetails) principal).getUsername();
            }
            if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                return ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
            }
        } catch (Exception ex) {
            log.debug("getCurrentUsername error", ex);
        }
        return null;
    }

    /**
     * 判断当前用户是否拥有指定权限
     * TODO: 从 SecurityContextHolder 获取权限列表并校验
     */
    public boolean hasPermission(String permission) {
        try {
            java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> auths = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getAuthorities();
            if (auths == null) return false;
            for (org.springframework.security.core.GrantedAuthority a : auths) {
                if (permission.equals(a.getAuthority())) return true;
            }
        } catch (Exception ex) {
            log.debug("hasPermission error", ex);
        }
        return false;
    }
}
