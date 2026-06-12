package com.example.wechatstore.config;

import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.AutoCertificateNotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * 微信支付配置
 * - 绑定 application.yaml 中 wxpay.* 属性
 * - 初始化 SDK 核心配置和 JSAPI 支付服务
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "wxpay")
public class WxPayConfig {

    private String appId;
    private String mchId;
    private String apiV3Key;
    private String privateKeyPath;
    private String merchantSerialNumber;
    private String notifyUrl;

    @Bean
    @Lazy
    public RSAAutoCertificateConfig rsaAutoCertificateConfig() {
        return new RSAAutoCertificateConfig.Builder()
                .merchantId(mchId)
                .privateKeyFromPath(privateKeyPath)
                .merchantSerialNumber(merchantSerialNumber)
                .apiV3Key(apiV3Key)
                .build();
    }

    @Bean
    @Lazy
    public JsapiServiceExtension jsapiServiceExtension(RSAAutoCertificateConfig config) {
        return new JsapiServiceExtension.Builder().config(config).build();
    }

    @Bean
    @Lazy
    public NotificationParser notificationParser() {
        AutoCertificateNotificationConfig notificationConfig = new AutoCertificateNotificationConfig.Builder()
                .merchantId(mchId)
                .privateKeyFromPath(privateKeyPath)
                .merchantSerialNumber(merchantSerialNumber)
                .apiV3Key(apiV3Key)
                .build();
        return new NotificationParser(notificationConfig);
    }
}
