package com.example.wechatstore.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.admin")
public class AdminAuthProperties {

    private String username;
    private String password;
    private String headerName = "X-Admin-Token";
    private String tokenSecret;
    private long ttlSeconds = 604800;
}
