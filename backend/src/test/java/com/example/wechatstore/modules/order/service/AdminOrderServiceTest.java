package com.example.wechatstore.modules.order.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.wechatstore.common.enums.OrderStatus;
import com.example.wechatstore.common.exception.BizException;
import com.example.wechatstore.modules.order.entity.OrderInfo;
import com.example.wechatstore.modules.order.mapper.OrderInfoMapper;
import com.example.wechatstore.modules.order.mapper.OrderItemMapper;
import com.example.wechatstore.modules.payment.mapper.PaymentRecordMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminOrderServiceTest {

    @BeforeAll
    static void initMybatisPlusTableInfo() {
        MybatisConfiguration configuration = new MybatisConfiguration();

        MapperBuilderAssistant assistant =
                new MapperBuilderAssistant(configuration, "");

        assistant.setCurrentNamespace(OrderInfoMapper.class.getName());

        TableInfoHelper.initTableInfo(assistant, OrderInfo.class);
    }

    @Mock
    private OrderInfoMapper orderInfoMapper;
    @Mock
    private OrderItemMapper orderItemMapper;
    @Mock
    private PaymentRecordMapper paymentRecordMapper;

    private AdminOrderService adminOrderService;

    @BeforeEach
    void setUp() {
        adminOrderService = new AdminOrderService(orderInfoMapper, orderItemMapper, paymentRecordMapper);
    }

    @Test
    void markDeliveringAllowsEmptyTrackingNo() {
        when(orderInfoMapper.selectOne(any())).thenReturn(order(OrderStatus.PAID));
        when(orderInfoMapper.update(isNull(), any())).thenReturn(1);

        adminOrderService.markDelivering("NO123", " ");

        LambdaUpdateWrapper<OrderInfo> wrapper = captureUpdateWrapper();
        Collection<Object> values = wrapper.getParamNameValuePairs().values();
        assertThat(values).doesNotContain("顺丰");
        assertThat(values).containsNull();
    }

    @Test
    void markDeliveringSavesSfTrackingNo() {
        when(orderInfoMapper.selectOne(any())).thenReturn(order(OrderStatus.PAID));
        when(orderInfoMapper.update(isNull(), any())).thenReturn(1);

        adminOrderService.markDelivering("NO123", " SF123456 ");

        Collection<Object> values = captureUpdateWrapper().getParamNameValuePairs().values();
        assertThat(values).contains("顺丰", "SF123456");
    }

    @Test
    void markDeliveringRejectsNonPaidOrder() {
        when(orderInfoMapper.selectOne(any())).thenReturn(order(OrderStatus.DELIVERING));

        assertThatThrownBy(() -> adminOrderService.markDelivering("NO123", "SF123456"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("当前订单状态不允许标记配送中");
        verify(orderInfoMapper, never()).update(any(), any());
    }

    private LambdaUpdateWrapper<OrderInfo> captureUpdateWrapper() {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<LambdaUpdateWrapper<OrderInfo>> captor =
                ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
        verify(orderInfoMapper).update(isNull(), captor.capture());
        return captor.getValue();
    }

    private OrderInfo order(String status) {
        OrderInfo order = new OrderInfo();
        order.setOrderNo("NO123");
        order.setStatus(status);
        return order;
    }
}
