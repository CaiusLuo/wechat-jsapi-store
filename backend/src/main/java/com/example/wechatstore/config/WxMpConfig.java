package com.example.wechatstore.config;

import lombok.Data;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "wx.mp")
public class WxMpConfig {

    private String appId;
    private String secret;
    private String token;
    private String aesKey;
    private String oauthCallbackUrl;

    /**
     * 构建微信公众平台客户端。
     * <p>
     * 这里只负责把配置项注入到 SDK 中，业务层直接注入 WxMpService 使用即可。
     * </p>
     */
    @Bean
    public WxMpService wxMpService() {
        WxMpDefaultConfigImpl config = new WxMpDefaultConfigImpl();
        config.setAppId(appId);
        config.setSecret(secret);

        WxMpServiceImpl service = new WxMpServiceImpl();
        service.setWxMpConfigStorage(config);
        return service;
    }
}
