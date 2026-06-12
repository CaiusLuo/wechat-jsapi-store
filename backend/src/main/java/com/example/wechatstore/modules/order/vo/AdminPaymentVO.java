package com.example.wechatstore.modules.order.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminPaymentVO(
        String transactionId,
        BigDecimal amount,
        Integer payStatus,
        String payStatusText,
        LocalDateTime payTime
) {
}
