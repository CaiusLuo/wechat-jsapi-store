package com.example.wechatstore.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.upload")
public class AppUploadProperties {

    private String baseDir = "./uploads";
    private String publicPrefix = "/uploads";
    private DataSize maxSize = DataSize.ofMegabytes(5);
}
