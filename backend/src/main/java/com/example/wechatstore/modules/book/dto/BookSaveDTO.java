package com.example.wechatstore.modules.book.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BookSaveDTO(
        @NotBlank String name,
        String subtitle,
        String coverUrl,
        @NotNull @DecimalMin(value = "0.00", inclusive = false) BigDecimal originalPrice,
        @NotNull @DecimalMin(value = "0.00", inclusive = false) BigDecimal price,
        String intro,
        @Min(0) Integer stock,
        Integer sort,
        @Min(0) @Max(1) Integer status
) {
}
