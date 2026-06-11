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
 * JWT 认证 + RBAC 权限控制：
 *   - /auth/** 放行（登录接口）
 *   - /swagger-ui/**, /v3/api-docs/**, /doc.html, /webjars/** 放行（API文档）
 *   - /h2-console/** 放行（开发数据库控制台）
 *   - 其他所有请求需要认证
 *   - 接口级权限由 @PreAuthorize 控制
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
                // 认证配置
                // 注意：server.servlet.context-path=/oa 会剥离前缀，Security 匹配的是剥离后的路径
                .authorizeHttpRequests(auth -> auth
                        // 认证接口放行
                        .requestMatchers("/auth/**").permitAll()
                        // API文档放行
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/v3/api-docs/**",
                                "/doc.html",
                                "/webjars/**"
                        ).permitAll()
                        // H2 控制台放行
                        .requestMatchers("/h2-console/**").permitAll()
                        // 静态资源放行
                        .requestMatchers("/", "/favicon.ico").permitAll()
                        // 其他所有请求需要认证（鉴权由 @PreAuthorize 控制）
                        .anyRequest().authenticated()
                )
                // H2 控制台需要 frame 同源策略放行
                .headers(headers ->
                        headers.frameOptions(frame -> frame.sameOrigin()))
                // 异常处理：未认证返回401，权限不足返回403
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(401);
                            response.getWriter().write("{\"code\":401,\"message\":\"登录已过期，请重新登录\",\"data\":null,\"timestamp\":" + System.currentTimeMillis() + "}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(403);
                            response.getWriter().write("{\"code\":403,\"message\":\"权限不足，无法访问该资源\",\"data\":null,\"timestamp\":" + System.currentTimeMillis() + "}");
                        })
                );

        // 在用户名/密码认证过滤器之前加入 JWT 认证过滤器
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
