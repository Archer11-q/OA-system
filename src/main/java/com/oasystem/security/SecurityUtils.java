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
        // TODO: return ((UserDetails) SecurityContextHolder.getContext()
        //         .getAuthentication().getPrincipal()).getUserId();
        log.warn("SecurityUtils.getCurrentUserId() 尚未实现，返回默认值");
        return 1L;
    }

    /**
     * 获取当前登录用户名
     * TODO: 从 SecurityContextHolder 获取
     */
    public String getCurrentUsername() {
        // TODO: return SecurityContextHolder.getContext().getAuthentication().getName();
        log.warn("SecurityUtils.getCurrentUsername() 尚未实现，返回默认值");
        return "admin";
    }

    /**
     * 判断当前用户是否拥有指定权限
     * TODO: 从 SecurityContextHolder 获取权限列表并校验
     */
    public boolean hasPermission(String permission) {
        log.warn("SecurityUtils.hasPermission() 尚未实现，默认返回 true");
        return true;
    }
}
