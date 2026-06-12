package com.example.wechatstore.modules.order.vo;

import com.example.wechatstore.modules.payment.vo.JsapiPayParamsVO;

import java.math.BigDecimal;

public record CreatePayOrderVO(
        String orderNo,
        BigDecimal payAmount,
        JsapiPayParamsVO payParams
) {
}
