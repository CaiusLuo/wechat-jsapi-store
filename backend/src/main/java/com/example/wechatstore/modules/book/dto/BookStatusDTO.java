package com.example.wechatstore.modules.book.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BookStatusDTO(
        @NotNull @Min(0) @Max(1) Integer status
) {
}
