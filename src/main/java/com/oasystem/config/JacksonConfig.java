package com.oasystem.config;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson 全局配置
 * <p>
 * 修复 spring.jackson.date-format 对 java.time.LocalDateTime 等
 * Java 8 时间类型不生效的问题。
 * <p>
 * 前端使用 value-format="YYYY-MM-DD HH:mm:ss"，
 * 此处配置 JavaTimeModule 以支持该格式的序列化与反序列化。
 */
@Configuration
public class JacksonConfig {

    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String TIME_PATTERN = "HH:mm:ss";
    private static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer javaTimeCustomizer() {
        return builder -> {
            JavaTimeModule module = new JavaTimeModule();

            // LocalDateTime ↔ "yyyy-MM-dd HH:mm:ss"
            module.addSerializer(LocalDateTime.class,
                    new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATETIME_PATTERN)));
            module.addDeserializer(LocalDateTime.class,
                    new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATETIME_PATTERN)));

            // LocalDate ↔ "yyyy-MM-dd"
            module.addSerializer(LocalDate.class,
                    new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE_PATTERN)));
            module.addDeserializer(LocalDate.class,
                    new LocalDateDeserializer(DateTimeFormatter.ofPattern(DATE_PATTERN)));

            // LocalTime ↔ "HH:mm:ss"
            module.addSerializer(LocalTime.class,
                    new LocalTimeSerializer(DateTimeFormatter.ofPattern(TIME_PATTERN)));
            module.addDeserializer(LocalTime.class,
                    new LocalTimeDeserializer(DateTimeFormatter.ofPattern(TIME_PATTERN)));

            builder.modules(module);
        };
    }
}
