package com.example.wechatstore.utils;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class OrderNoGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private final SecureRandom random = new SecureRandom();

    public String nextOrderNo() {
        return "B" + LocalDateTime.now().format(FORMATTER) + String.format("%06d", random.nextInt(1_000_000));
    }
}
