package com.oasystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.oasystem.security.JwtAuthenticationFilter;

/**
 * Spring Security 安全配置
 * <p>
 * 当前阶段（开发初期）：放行所有请求，仅配置基础组件。
 * 后续逐步开启：
 *   1. 登录接口 + JWT 认证
 *   2. RBAC 权限控制（基于菜单表）
 *   3. 接口级别的权限注解
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                // 关闭 CSRF（前后端分离 + RESTful API 无状态）
                .csrf(AbstractHttpConfigurer::disable)
                // 无状态会话（不使用 HttpSession）
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 放行所有 API（认证由 JwtAuthenticationFilter 处理，鉴权由 @PreAuthorize 控制）
                // 注意：server.servlet.context-path=/oa 会剥离前缀，Security 匹配的是剥离后的路径
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/auth/**",
                                "/dashboard/**",
                                "/system/**",
                                "/attendance/**",
                                "/approval/**",
                                "/notice/**",
                                "/schedule/**",
                                "/expense/**",
                                "/file/**",
                                "/log/**",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/v3/api-docs/**",
                                "/doc.html",
                                "/webjars/**",
                                "/h2-console/**",
                                "/favicon.ico"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                // H2 控制台需要 frame 同源策略放行
                .headers(headers ->
                        headers.frameOptions(frame -> frame.sameOrigin()));

        // 在用户名/密码认证过滤器之前加入 JWT 认证过滤器
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
