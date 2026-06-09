package com.oasystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * <p>
 * 用于注册拦截器、资源处理器、参数解析器等。
 *
 * TODO: 添加登录拦截器、权限拦截器等。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // TODO: 注册 JWT 拦截器（或使用 Spring Security Filter）
    // TODO: 配置静态资源映射
    // TODO: 配置统一前缀等
}
