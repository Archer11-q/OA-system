package com.oasystem.log.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * <p>
 * 标注在 Controller 方法上，自动记录操作日志。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    /** 操作模块（如：用户管理、考勤管理） */
    String module() default "";

    /** 操作描述（如：新增用户、签到） */
    String value() default "";
}
