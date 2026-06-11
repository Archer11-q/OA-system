package com.oasystem.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * JWT 认证过滤器
 * <p>
 * 每个请求进入时，从 Header 中提取 JWT Token 并校验，
 * 校验通过后将用户信息设置到 SecurityContext。
 *
 * TODO: 完成 Spring Security 集成后，在此注入 UserDetailsService 并设置 SecurityContext。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @Value("${jwt.header}")
    private String tokenHeader;

    @Value("${jwt.token-prefix}")
    private String tokenPrefix;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);

        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            String username = jwtTokenProvider.getUsernameFromToken(token);

            log.debug("JWT认证成功: userId={}, username={}", userId, username);

            try {
                org.springframework.security.core.userdetails.UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception ex) {
                // 加载用户权限失败时，不要创建空权限的认证，否则用户会以无权限状态登录
                // 改为不设置认证上下文，让请求以未认证状态继续，触发 401 返回明确错误
                log.error("加载用户详情失败（将拒绝本次请求）: userId={}, username={}, error={}",
                        userId, username, ex.getMessage(), ex);
                // 不设置 SecurityContext，后续 SecurityConfig 会返回 401
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中提取 JWT Token
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(tokenHeader);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(tokenPrefix)) {
            return bearerToken.substring(tokenPrefix.length()).trim();
        }
        return null;
    }
}
