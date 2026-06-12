package com.example.wechatstore.modules.order.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminOrderListVO(
        String orderNo,
        String status,
        String statusText,
        BigDecimal totalAmount,
        BigDecimal payAmount,
        String receiverName,
        String phone,
        String school,
        String province,
        String city,
        String district,
        String detailAddress,
        String itemSummary,
        String trackingCompany,
        String trackingNo,
        LocalDateTime createTime,
        LocalDateTime payTime,
        LocalDateTime deliverTime,
        LocalDateTime finishTime
) {
}
