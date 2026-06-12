package com.example.wechatstore.modules.order.controller;

import com.example.wechatstore.common.result.Result;
import com.example.wechatstore.modules.order.dto.CreatePayOrderDTO;
import com.example.wechatstore.modules.order.service.OrderService;
import com.example.wechatstore.modules.order.vo.CreatePayOrderVO;
import com.example.wechatstore.modules.order.vo.CreatedOrderVO;
import com.example.wechatstore.modules.order.vo.OrderDetailVO;
import com.example.wechatstore.modules.payment.service.PaymentService;
import com.example.wechatstore.modules.payment.vo.JsapiPayParamsVO;
import com.example.wechatstore.utils.H5AuthResolver;
import com.example.wechatstore.utils.H5TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/h5/orders")
public class H5OrderController {

    private final H5AuthResolver h5AuthResolver;
    private final OrderService orderService;
    private final PaymentService paymentService;

    public H5OrderController(
            H5AuthResolver h5AuthResolver,
            OrderService orderService,
            PaymentService paymentService
    ) {
        this.h5AuthResolver = h5AuthResolver;
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    @PostMapping("/pay")
    public Result<CreatePayOrderVO> createOrderAndPay(
            @Valid @RequestBody CreatePayOrderDTO dto,
            HttpServletRequest request
    ) {
        H5TokenService.H5UserPrincipal user = h5AuthResolver.requireUser(request);
        CreatedOrderVO created = orderService.createOrder(user.userId(), user.openid(), dto);
        JsapiPayParamsVO payParams = paymentService.createJsapiPayParams(created.order(), user.openid());
        return Result.ok(new CreatePayOrderVO(
                created.order().getOrderNo(),
                created.order().getPayAmount(),
                payParams
        ));
    }

    @GetMapping("/{orderNo}")
    public Result<OrderDetailVO> getOrderDetail(
            @PathVariable String orderNo,
            HttpServletRequest request
    ) {
        H5TokenService.H5UserPrincipal user = h5AuthResolver.requireUser(request);
        return Result.ok(orderService.getOrderDetail(user.userId(), orderNo));
    }

    @GetMapping("/my")
    public Result<List<OrderDetailVO>> listMyOrders(
            @RequestParam(required = false, defaultValue = "false") boolean unfinished,
            HttpServletRequest request
    ) {
        H5TokenService.H5UserPrincipal user = h5AuthResolver.requireUser(request);
        return Result.ok(orderService.listMyOrders(user.userId(), unfinished));
    }

    @PostMapping("/{orderNo}/cancel")
    public Result<Void> cancelMyOrder(
            @PathVariable String orderNo,
            HttpServletRequest request
    ) {
        H5TokenService.H5UserPrincipal user = h5AuthResolver.requireUser(request);
        orderService.cancelMyOrder(user.userId(), orderNo);
        return Result.ok();
    }
}
