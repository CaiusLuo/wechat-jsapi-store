package com.example.wechatstore.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.cors")
public class AppCorsProperties {

    private List<String> allowedOrigins = List.of(
            "http://localhost:5173",
            "http://127.0.0.1:5173"
    );
}
