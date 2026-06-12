package com.example.wechatstore.modules.order.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.wechatstore.common.result.Result;
import com.example.wechatstore.modules.order.dto.AdminDeliverOrderDTO;
import com.example.wechatstore.modules.order.dto.AdminOrderQueryDTO;
import com.example.wechatstore.modules.order.dto.AdminOrderStatusDTO;
import com.example.wechatstore.modules.order.service.AdminOrderService;
import com.example.wechatstore.modules.order.vo.AdminOrderDetailVO;
import com.example.wechatstore.modules.order.vo.AdminOrderListVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    public AdminOrderController(AdminOrderService adminOrderService) {
        this.adminOrderService = adminOrderService;
    }

    @GetMapping
    public Result<Page<AdminOrderListVO>> listOrders(AdminOrderQueryDTO query) {
        return Result.ok(adminOrderService.listOrders(query));
    }

    @GetMapping("/{orderNo}")
    public Result<AdminOrderDetailVO> getOrderDetail(@PathVariable String orderNo) {
        return Result.ok(adminOrderService.getOrderDetail(orderNo));
    }

    @PostMapping("/{orderNo}/deliver")
    public Result<Void> markDelivering(
            @PathVariable String orderNo,
            @RequestBody(required = false) AdminDeliverOrderDTO dto
    ) {
        adminOrderService.markDelivering(orderNo, dto == null ? null : dto.getTrackingNo());
        return Result.ok();
    }

    @PostMapping("/{orderNo}/finish")
    public Result<Void> markFinished(@PathVariable String orderNo) {
        adminOrderService.markFinished(orderNo);
        return Result.ok();
    }

    @PostMapping("/{orderNo}/cancel")
    public Result<Void> cancelOrder(@PathVariable String orderNo) {
        adminOrderService.cancelOrder(orderNo);
        return Result.ok();
    }

    @PutMapping("/{orderNo}/status")
    public Result<Void> changeStatus(@PathVariable String orderNo, @Valid @RequestBody AdminOrderStatusDTO dto) {
        adminOrderService.changeStatus(orderNo, dto.getStatus());
        return Result.ok();
    }
}
