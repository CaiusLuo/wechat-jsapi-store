package com.example.wechatstore.modules.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderBookDTO(
        @NotNull Long bookId,
        @NotNull @Min(1) Integer quantity
) {
}
