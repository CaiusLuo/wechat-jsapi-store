package com.example.wechatstore.modules.payment.vo;

public record JsapiPayParamsVO(
        String appId,
        String timeStamp,
        String nonceStr,
        String packageValue,
        String signType,
        String paySign
) {
}
