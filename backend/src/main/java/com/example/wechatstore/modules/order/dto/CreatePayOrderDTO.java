package com.example.wechatstore.modules.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreatePayOrderDTO(
        @NotBlank String receiverName,
        @NotBlank String phone,
        @NotBlank String school,
        @NotBlank String province,
        @NotBlank String city,
        @NotBlank String district,
        @NotBlank String detailAddress,
        String remark,
        @Valid @NotEmpty List<OrderBookDTO> items
) {
}
