package com.example.wechatstore.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "h5.auth")
public class H5AuthProperties {

    /**
     * H5 登录态 Cookie 名称。
     * 默认值用于前后端统一识别当前登录 token。
     */
    private String cookieName = "WECHAT_STORE_H5_TOKEN";

    /**
     * H5 Token 签名密钥，用于生成和校验登录态。
     */
    private String tokenSecret;

    /**
     * H5 登录态有效期，单位：秒。
     */
    private long ttlSeconds = 604800;
}
