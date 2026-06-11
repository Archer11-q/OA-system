package com.oasystem.common.exception;

import com.oasystem.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 * <p>
 * 捕获所有 Controller 层抛出的异常，
 * 统一转换为 Result 格式返回给前端。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常 */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /** 参数校验异常（@Valid） */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(Exception e) {
        String message = "参数校验失败";
        if (e instanceof MethodArgumentNotValidException ex) {
            if (ex.hasErrors() && ex.getFieldError() != null) {
                message = ex.getFieldError().getDefaultMessage();
            }
        }
        log.warn("参数校验异常: {}", message);
        return Result.fail(400, message);
    }

    /** 认证异常 */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleAuthenticationException(AuthenticationException e) {
        log.warn("认证失败: {}", e.getMessage());
        return Result.unauthorized("登录已过期或未登录，请重新登录");
    }

    /** 权限不足 */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("权限不足: {}", e.getMessage());
        return Result.forbidden("权限不足，无法访问该资源");
    }

    /** 其他未知异常 */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常: ", e);
        // 返回具体错误信息，方便调试（生产环境可改为通用提示）
        String detail = e.getMessage() != null ? e.getMessage() : "未知错误";
        return Result.fail("系统繁忙，请稍后重试（" + detail + "）");
    }
}
