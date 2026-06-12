package com.example.wechatstore.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AppUploadProperties uploadProperties;
    private final AppCorsProperties corsProperties;

    public WebMvcConfig(AppUploadProperties uploadProperties, AppCorsProperties corsProperties) {
        this.uploadProperties = uploadProperties;
        this.corsProperties = corsProperties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (corsProperties.getAllowedOrigins().contains("*")) {
            throw new IllegalStateException("CORS wildcard origin is not allowed with credentials");
        }
        registry.addMapping("/api/**")
                .allowedOrigins(corsProperties.getAllowedOrigins().toArray(String[]::new))
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(uploadProperties.getBaseDir()).toAbsolutePath().normalize();
        registry.addResourceHandler(normalizePublicPrefix(uploadProperties.getPublicPrefix()) + "/**")
                .addResourceLocations(asDirectoryLocation(uploadPath));
    }

    private String asDirectoryLocation(Path path) {
        String location = path.toUri().toString();
        return location.endsWith("/") ? location : location + "/";
    }

    private String normalizePublicPrefix(String publicPrefix) {
        if (publicPrefix == null || publicPrefix.isBlank()) {
            return "/uploads";
        }
        String normalized = publicPrefix.startsWith("/") ? publicPrefix : "/" + publicPrefix;
        if (normalized.endsWith("/")) {
            return normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
