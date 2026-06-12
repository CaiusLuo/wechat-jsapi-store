package com.example.wechatstore.modules.payment.controller;

import com.example.wechatstore.modules.payment.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/pay")
/**
 * 支付通知控制器。
 * <p>
 * 负责接收微信支付异步回调，并交给支付服务完成业务处理。
 * </p>
 */
public class PayNotifyController {

    private final PaymentService paymentService;

    public PayNotifyController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * 微信支付异步通知入口。
     * <p>
     * 微信会通过请求头传入验签参数，请求体携带原始通知内容。
     * </p>
     *
     * @return 成功返回 SUCCESS，失败返回 FAIL
     */
    @PostMapping("/notify")
    public ResponseEntity<Map<String, String>> notify(
            @RequestHeader("Wechatpay-Serial") String serialNumber,
            @RequestHeader("Wechatpay-Nonce") String nonce,
            @RequestHeader("Wechatpay-Timestamp") String timestamp,
            @RequestHeader("Wechatpay-Signature") String signature,
            @RequestBody String body
    ) {
        try {
            paymentService.handlePayNotify(serialNumber, nonce, timestamp, signature, body);
            return ResponseEntity.ok(Map.of("code", "SUCCESS", "message", "成功"));
        } catch (Exception ex) {
            log.warn("wechat pay notify failed: {}", ex.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("code", "FAIL", "message", "失败"));
        }
    }
}
