package com.example.wechatstore.modules.user.vo;

public record UserProfileVO(
        String openid,
        String nickname,
        String avatar,
        String receiverName,
        String phone,
        String school,
        String province,
        String city,
        String district,
        String detailAddress,
        boolean profileCompleted
) {
}
