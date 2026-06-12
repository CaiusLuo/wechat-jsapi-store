package com.example.wechatstore.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson 序列化配置
 * - Long/BigInteger → String（防止前端 JS 精度丢失）
 * - LocalDateTime / LocalDate 统一格式
 */
@Configuration
public class JacksonConfig {

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String DATE_PATTERN = "yyyy-MM-dd";

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            // Long / BigInteger → String
            builder.serializerByType(Long.class, ToStringSerializer.instance);
            builder.serializerByType(Long.TYPE, ToStringSerializer.instance);
            builder.serializerByType(BigInteger.class, ToStringSerializer.instance);

            // LocalDateTime
            DateTimeFormatter dateTimeFmt = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
            builder.serializerByType(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFmt));
            builder.deserializerByType(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFmt));

            // LocalDate
            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern(DATE_PATTERN);
            builder.serializerByType(LocalDate.class, new LocalDateSerializer(dateFmt));
            builder.deserializerByType(LocalDate.class, new LocalDateDeserializer(dateFmt));
        };
    }
}
