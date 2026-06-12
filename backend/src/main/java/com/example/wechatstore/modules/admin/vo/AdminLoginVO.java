package com.example.wechatstore.modules.admin.vo;

public record AdminLoginVO(
        String token,
        Long expiresAt,
        String username
) {
}
