package com.example.wechatstore.modules.admin.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminWeeklySalesVO {

    private String date;
    private Long salesVolume;
    private BigDecimal salesAmount;
}
