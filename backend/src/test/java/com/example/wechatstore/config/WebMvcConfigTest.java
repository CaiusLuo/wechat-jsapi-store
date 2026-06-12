package com.example.wechatstore.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WebMvcConfigTest {

    @Test
    void rejectsWildcardOriginWhenCredentialsAreEnabled() {
        AppCorsProperties corsProperties = new AppCorsProperties();
        corsProperties.setAllowedOrigins(List.of("*"));
        WebMvcConfig config = new WebMvcConfig(new AppUploadProperties(), corsProperties);

        assertThatThrownBy(() -> config.addCorsMappings(new CorsRegistry()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("wildcard");
    }
}
