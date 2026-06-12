package com.example.wechatstore.modules.order.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AdminOrderDetailVO(
        String orderNo,
        String status,
        String statusText,
        Integer payStatus,
        String payStatusText,
        BigDecimal totalAmount,
        BigDecimal payAmount,
        String receiverName,
        String phone,
        String school,
        String province,
        String city,
        String district,
        String detailAddress,
        String remark,
        String trackingCompany,
        String trackingNo,
        List<OrderItemVO> items,
        AdminPaymentVO payment,
        LocalDateTime createTime,
        LocalDateTime payTime,
        LocalDateTime deliverTime,
        LocalDateTime finishTime
) {
}
