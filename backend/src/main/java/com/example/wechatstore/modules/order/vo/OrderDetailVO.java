package com.example.wechatstore.modules.order.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailVO(
        String orderNo,
        String status,
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
        List<OrderItemVO> items,
        LocalDateTime payTime,
        LocalDateTime createTime,
        LocalDateTime deliverTime,
        LocalDateTime finishTime
) {
}
