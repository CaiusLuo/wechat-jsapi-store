package com.example.wechatstore.modules.payment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.wechatstore.common.enums.OrderStatus;
import com.example.wechatstore.common.enums.PaymentStatus;
import com.example.wechatstore.common.exception.BizException;
import com.example.wechatstore.config.WxPayConfig;
import com.example.wechatstore.modules.order.entity.OrderInfo;
import com.example.wechatstore.modules.order.mapper.OrderInfoMapper;
import com.example.wechatstore.modules.payment.entity.PaymentRecord;
import com.example.wechatstore.modules.payment.mapper.PaymentRecordMapper;
import com.example.wechatstore.modules.payment.vo.JsapiPayParamsVO;
import com.example.wechatstore.utils.MoneyUtils;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import com.wechat.pay.java.service.payments.model.Transaction;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Service
/**
 * 支付服务。
 * <p>
 * 负责生成微信 JSAPI 支付参数，并处理微信支付异步通知。
 * </p>
 */
public class PaymentService {

    private final WxPayConfig wxPayConfig;
    private final JsapiServiceExtension jsapiServiceExtension;
    private final NotificationParser notificationParser;
    private final OrderInfoMapper orderInfoMapper;
    private final PaymentRecordMapper paymentRecordMapper;

    public PaymentService(
            WxPayConfig wxPayConfig,
            @Lazy JsapiServiceExtension jsapiServiceExtension,
            @Lazy NotificationParser notificationParser,
            OrderInfoMapper orderInfoMapper,
            PaymentRecordMapper paymentRecordMapper
    ) {
        this.wxPayConfig = wxPayConfig;
        this.jsapiServiceExtension = jsapiServiceExtension;
        this.notificationParser = notificationParser;
        this.orderInfoMapper = orderInfoMapper;
        this.paymentRecordMapper = paymentRecordMapper;
    }

    /**
     * 生成微信 JSAPI 支付参数。
     * <p>
     * 前端拿到这些参数后，可以直接唤起微信支付。
     * </p>
     *
     * @param order  订单信息，包含订单号和应付金额
     * @param openid 当前微信用户的 openid
     * @return JSAPI 支付参数
     */
    public JsapiPayParamsVO createJsapiPayParams(OrderInfo order, String openid) {
        Amount amount = new Amount();
        amount.setTotal(MoneyUtils.yuanToCents(order.getPayAmount()));
        amount.setCurrency("CNY");

        Payer payer = new Payer();
        payer.setOpenid(openid);

        PrepayRequest request = new PrepayRequest();
        request.setAppid(wxPayConfig.getAppId());
        request.setMchid(wxPayConfig.getMchId());
        request.setDescription("书籍订单 " + order.getOrderNo());
        request.setOutTradeNo(order.getOrderNo());
        request.setNotifyUrl(wxPayConfig.getNotifyUrl());
        request.setAmount(amount);
        request.setPayer(payer);

        PrepayWithRequestPaymentResponse response = jsapiServiceExtension.prepayWithRequestPayment(request);
        return new JsapiPayParamsVO(
                response.getAppId(),
                response.getTimeStamp(),
                response.getNonceStr(),
                response.getPackageVal(),
                response.getSignType(),
                response.getPaySign()
        );
    }

    /**
     * 处理微信支付异步通知。
     * <p>
     * 核心流程：解析通知 -> 校验商户 -> 校验订单与金额 -> 更新支付记录 -> 更新订单状态。
     * </p>
     *
     * @param serialNumber 微信平台证书序列号
     * @param nonce        通知随机串
     * @param timestamp    通知时间戳
     * @param signature    通知签名
     * @param body         原始回调报文
     */
    @Transactional(rollbackFor = Exception.class)
    public void handlePayNotify(
            String serialNumber,
            String nonce,
            String timestamp,
            String signature,
            String body
    ) {
        RequestParam requestParam = new RequestParam.Builder()
                .serialNumber(serialNumber)
                .nonce(nonce)
                .timestamp(timestamp)
                .signature(signature)
                .body(body)
                .build();
        Transaction transaction = notificationParser.parse(requestParam, Transaction.class);
        if (transaction.getTradeState() != Transaction.TradeStateEnum.SUCCESS) {
            return;
        }

        // 先确认通知确实来自当前商户，避免处理错账单。
        validateMerchant(transaction);

        OrderInfo order = orderInfoMapper.selectOne(new LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getOrderNo, transaction.getOutTradeNo())
                .last("limit 1"));
        if (order == null) {
            throw new BizException("order not found");
        }

        // 以订单实付金额为准，防止回调金额与本地订单不一致。
        int expectedAmount = MoneyUtils.yuanToCents(order.getPayAmount());
        int paidAmount = transaction.getAmount().getPayerTotal() != null
                ? transaction.getAmount().getPayerTotal()
                : transaction.getAmount().getTotal();
        if (expectedAmount != paidAmount) {
            throw new BizException("wechat pay amount mismatch");
        }

        PaymentRecord paymentRecord = paymentRecordMapper.selectOne(new LambdaQueryWrapper<PaymentRecord>()
                .eq(PaymentRecord::getOrderNo, order.getOrderNo())
                .last("limit 1"));
        if (paymentRecord == null) {
            throw new BizException("payment record not found");
        }

        LocalDateTime payTime = parsePayTime(transaction.getSuccessTime());
        paymentRecord.setTransactionId(transaction.getTransactionId());
        paymentRecord.setMchid(transaction.getMchid());
        paymentRecord.setPayStatus(PaymentStatus.SUCCESS);
        paymentRecord.setPayTime(payTime);
        paymentRecordMapper.updateById(paymentRecord);

        // 已进入支付后续状态的订单直接返回，保证重复通知和弱网重试幂等。
        if (OrderStatus.PAID.equals(order.getStatus())
                || OrderStatus.DELIVERING.equals(order.getStatus())
                || OrderStatus.FINISHED.equals(order.getStatus())) {
            return;
        }
        if (!OrderStatus.CREATED.equals(order.getStatus())) {
            throw new BizException("order status cannot be paid: " + order.getStatus());
        }

        int rows = orderInfoMapper.update(null, new LambdaUpdateWrapper<OrderInfo>()
                .eq(OrderInfo::getOrderNo, order.getOrderNo())
                .eq(OrderInfo::getStatus, OrderStatus.CREATED)
                .set(OrderInfo::getStatus, OrderStatus.PAID)
                .set(OrderInfo::getPayStatus, PaymentStatus.SUCCESS)
                .set(OrderInfo::getPayTime, payTime));
        if (rows == 0) {
            OrderInfo latest = orderInfoMapper.selectOne(new LambdaQueryWrapper<OrderInfo>()
                    .eq(OrderInfo::getOrderNo, order.getOrderNo())
                    .last("limit 1"));
            if (latest != null && (OrderStatus.PAID.equals(latest.getStatus())
                    || OrderStatus.DELIVERING.equals(latest.getStatus())
                    || OrderStatus.FINISHED.equals(latest.getStatus()))) {
                return;
            }
            throw new BizException("order status changed, pay notify ignored");
        }
    }

    /**
     * 校验支付通知中的商户身份，防止错误通知进入当前系统。
     */
    private void validateMerchant(Transaction transaction) {
        if (!wxPayConfig.getAppId().equals(transaction.getAppid())) {
            throw new BizException("wechat pay appid mismatch");
        }
        if (!wxPayConfig.getMchId().equals(transaction.getMchid())) {
            throw new BizException("wechat pay mchid mismatch");
        }
    }

    /**
     * 解析支付成功时间；如果微信未返回时间，则回退到当前时间。
     */
    private LocalDateTime parsePayTime(String successTime) {
        if (successTime == null) {
            return LocalDateTime.now();
        }
        return LocalDateTime.ofInstant(OffsetDateTime.parse(successTime).toInstant(), ZoneId.systemDefault());
    }
}
