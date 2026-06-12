package com.example.wechatstore.modules.h5config.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.wechatstore.modules.h5config.dto.H5ConfigDTO;
import com.example.wechatstore.modules.h5config.entity.H5Config;
import com.example.wechatstore.modules.h5config.mapper.H5ConfigMapper;
import com.example.wechatstore.modules.h5config.vo.H5ConfigVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class H5ConfigService {

    private static final String DEFAULT_SITE_TITLE = "教材购书服务";
    private static final String DEFAULT_SITE_SUBTITLE = "个人信息与教材订购入口";
    private static final String DEFAULT_SERVICE_WECHAT = "请联系管理员";
    private static final String DEFAULT_SERVICE_PHONE = "待配置";
    private static final String DEFAULT_WORK_TIME = "09:00 - 18:00";
    private static final String DEFAULT_NOTICE_TEXT = "下单后请保持电话畅通，配送信息以后续通知为准。";

    private final H5ConfigMapper h5ConfigMapper;

    public H5ConfigService(H5ConfigMapper h5ConfigMapper) {
        this.h5ConfigMapper = h5ConfigMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public H5ConfigVO getConfig() {
        return toVO(findOrCreateConfig());
    }

    @Transactional(rollbackFor = Exception.class)
    public H5ConfigVO updateConfig(H5ConfigDTO dto) {
        H5Config config = findOrCreateConfig();
        config.setSiteTitle(valueOrDefault(dto.siteTitle(), DEFAULT_SITE_TITLE));
        config.setSiteSubtitle(valueOrDefault(dto.siteSubtitle(), DEFAULT_SITE_SUBTITLE));
        config.setServiceWechat(valueOrDefault(dto.serviceWechat(), DEFAULT_SERVICE_WECHAT));
        config.setServicePhone(valueOrDefault(dto.servicePhone(), DEFAULT_SERVICE_PHONE));
        config.setWorkTime(valueOrDefault(dto.workTime(), DEFAULT_WORK_TIME));
        config.setNoticeText(valueOrDefault(dto.noticeText(), DEFAULT_NOTICE_TEXT));
        h5ConfigMapper.updateById(config);
        return toVO(config);
    }

    private H5Config findOrCreateConfig() {
        H5Config existing = h5ConfigMapper.selectOne(new LambdaQueryWrapper<H5Config>()
                .orderByAsc(H5Config::getId)
                .last("limit 1"));
        if (existing != null) {
            return existing;
        }

        H5Config config = new H5Config();
        config.setSiteTitle(DEFAULT_SITE_TITLE);
        config.setSiteSubtitle(DEFAULT_SITE_SUBTITLE);
        config.setServiceWechat(DEFAULT_SERVICE_WECHAT);
        config.setServicePhone(DEFAULT_SERVICE_PHONE);
        config.setWorkTime(DEFAULT_WORK_TIME);
        config.setNoticeText(DEFAULT_NOTICE_TEXT);
        h5ConfigMapper.insert(config);
        return config;
    }

    private String valueOrDefault(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    private H5ConfigVO toVO(H5Config config) {
        return new H5ConfigVO(
                valueOrDefault(config.getSiteTitle(), DEFAULT_SITE_TITLE),
                valueOrDefault(config.getSiteSubtitle(), DEFAULT_SITE_SUBTITLE),
                valueOrDefault(config.getServiceWechat(), DEFAULT_SERVICE_WECHAT),
                valueOrDefault(config.getServicePhone(), DEFAULT_SERVICE_PHONE),
                valueOrDefault(config.getWorkTime(), DEFAULT_WORK_TIME),
                valueOrDefault(config.getNoticeText(), DEFAULT_NOTICE_TEXT)
        );
    }
}
