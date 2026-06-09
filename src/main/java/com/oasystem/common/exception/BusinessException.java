package com.oasystem.common.exception;

import lombok.Getter;

/**
 * 业务异常
 * <p>
 * 所有业务逻辑异常统一用此类抛出，
 * 由 GlobalExceptionHandler 统一捕获处理。
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 异常状态码 */
    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
    }
}
