package com.example.wechatstore.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.example.wechatstore.modules.**.mapper")
public class MybatisPlusConfig {
}
