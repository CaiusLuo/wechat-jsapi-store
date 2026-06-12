package com.example.wechatstore.modules.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order_info")
public class OrderInfo {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private String status;
    private Integer payStatus;
    private String receiverName;
    private String phone;
    private String school;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private String remark;
    private String trackingCompany;
    private String trackingNo;
    private LocalDateTime payTime;
    private LocalDateTime deliverTime;
    private LocalDateTime finishTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
