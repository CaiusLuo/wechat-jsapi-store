package com.example.wechatstore.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j / SpringDoc API 文档配置
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("WeChat JSAPI Store API")
                        .version("1.0")
                        .description("微信 JSAPI 商城参考实现接口文档"));
    }

    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("all")
                .packagesToScan("com.example.wechatstore")
                .build();
    }
}
