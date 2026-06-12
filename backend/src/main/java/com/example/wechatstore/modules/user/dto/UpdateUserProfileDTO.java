package com.example.wechatstore.modules.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserProfileDTO(
        String nickname,
        String avatar,
        @NotBlank String receiverName,
        @NotBlank String phone,
        @NotBlank String school,
        @NotBlank String province,
        @NotBlank String city,
        @NotBlank String district,
        @NotBlank String detailAddress
) {
}
