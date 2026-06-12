package com.example.wechatstore.modules.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment_record")
public class PaymentRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private String orderNo;
    private String transactionId;
    private String mchid;
    private String payChannel;
    private BigDecimal amount;
    private Integer payStatus;
    private String notifyContent;
    private LocalDateTime payTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
