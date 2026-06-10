package com.oasystem.log.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oasystem.log.annotation.Log;
import com.oasystem.log.entity.OperLog;
import com.oasystem.log.mapper.OperLogMapper;
import com.oasystem.security.JwtUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * 操作日志 AOP 切面
 * <p>
 * 拦截标注了 @Log 注解的方法，自动记录操作日志。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperLogAspect {

    private final OperLogMapper operLogMapper;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    /** 返回结果最大长度（字符） */
    private static final int RESULT_MAX_LENGTH = 500;

    @Around("@annotation(operLog)")
    public Object around(ProceedingJoinPoint joinPoint, Log operLog) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getDeclaringType().getSimpleName() + "." + signature.getName();

        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String url = "";
        String requestMethod = "";
        String ip = "";
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            url = request.getRequestURI();
            requestMethod = request.getMethod();
            ip = getClientIp(request);
        }

        // 获取当前用户
        Long userId = null;
        String username = null;
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof JwtUserDetails) {
                JwtUserDetails user = (JwtUserDetails) principal;
                userId = user.getId();
                username = user.getUsername();
            }
        } catch (Exception ignored) {
        }

        // 请求参数
        String params = "";
        try {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                // 过滤掉 HttpServletRequest/Response 等非业务参数
                Object[] filteredArgs = new Object[args.length];
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof HttpServletRequest || args[i] instanceof jakarta.servlet.http.HttpServletResponse) {
                        filteredArgs[i] = "[request/response]";
                    } else {
                        filteredArgs[i] = args[i];
                    }
                }
                params = OBJECT_MAPPER.writeValueAsString(filteredArgs);
                if (params.length() > RESULT_MAX_LENGTH) {
                    params = params.substring(0, RESULT_MAX_LENGTH) + "...";
                }
            }
        } catch (Exception ignored) {
        }

        // 执行目标方法
        Object result = null;
        int status = 1; // 成功
        String errorMsg = null;
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            status = 0; // 失败
            errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.length() > 1000) {
                errorMsg = errorMsg.substring(0, 1000);
            }
            throw e; // 继续抛出，让全局异常处理器处理
        } finally {
            long costTime = System.currentTimeMillis() - startTime;

            // 保存日志
            try {
                OperLog logEntry = new OperLog();
                logEntry.setUserId(userId);
                logEntry.setUsername(username);
                logEntry.setModule(operLog.module());
                logEntry.setOperation(operLog.value());
                logEntry.setMethod(methodName);
                logEntry.setRequestMethod(requestMethod);
                logEntry.setUrl(url);
                logEntry.setIp(ip);
                logEntry.setRequestParams(params);
                logEntry.setCostTime(costTime);
                logEntry.setStatus(status);
                logEntry.setErrorMsg(errorMsg);
                logEntry.setCreateTime(LocalDateTime.now());

                // 记录返回结果（截断）
                if (status == 1 && result != null) {
                    try {
                        String resultStr = OBJECT_MAPPER.writeValueAsString(result);
                        if (resultStr.length() > RESULT_MAX_LENGTH) {
                            resultStr = resultStr.substring(0, RESULT_MAX_LENGTH) + "...";
                        }
                        logEntry.setResult(resultStr);
                    } catch (Exception ignored) {
                    }
                }

                operLogMapper.insert(logEntry);
            } catch (Exception e) {
                // 日志记录失败不影响业务
                log.warn("操作日志记录失败: {}", e.getMessage());
            }
        }

        return result;
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
