package com.example.wechatstore.modules.order.dto;

import lombok.Data;

@Data
public class AdminOrderQueryDTO {

    private Integer page;
    private Integer size;
    private String orderNo;
    private String phone;
    private String receiverName;
    private String status;
    private String startTime;
    private String endTime;
}
