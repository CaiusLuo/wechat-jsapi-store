package com.example.wechatstore.modules.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminLoginDTO(
        @NotBlank String username,
        @NotBlank String password
) {
}
