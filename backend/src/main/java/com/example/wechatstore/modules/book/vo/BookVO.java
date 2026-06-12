package com.example.wechatstore.modules.book.vo;

import java.math.BigDecimal;

public record BookVO(
        Long id,
        String name,
        String subtitle,
        String coverUrl,
        BigDecimal originalPrice,
        BigDecimal price,
        String intro,
        Integer stock,
        Integer sort,
        Integer status
) {
}
