package com.example.wechatstore.modules.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AdminDashboardOverviewVO {

    private Long todayOrderCount;
    private Long pendingDeliveryCount;
    private Long onSaleBookCount;
    private List<AdminWeeklySalesVO> weeklySales;
}
