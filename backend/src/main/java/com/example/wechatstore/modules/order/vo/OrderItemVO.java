package com.example.wechatstore.modules.order.vo;

import java.math.BigDecimal;

public record OrderItemVO(
        Long bookId,
        String bookName,
        String coverUrl,
        BigDecimal price,
        Integer quantity,
        BigDecimal subtotal
) {
}
