package com.example.wechatstore.modules.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.wechatstore.common.enums.OrderStatus;
import com.example.wechatstore.common.enums.PaymentStatus;
import com.example.wechatstore.common.exception.BizException;
import com.example.wechatstore.modules.order.dto.AdminOrderQueryDTO;
import com.example.wechatstore.modules.order.entity.OrderInfo;
import com.example.wechatstore.modules.order.entity.OrderItem;
import com.example.wechatstore.modules.order.mapper.OrderInfoMapper;
import com.example.wechatstore.modules.order.mapper.OrderItemMapper;
import com.example.wechatstore.modules.order.vo.AdminOrderDetailVO;
import com.example.wechatstore.modules.order.vo.AdminOrderListVO;
import com.example.wechatstore.modules.order.vo.AdminPaymentVO;
import com.example.wechatstore.modules.order.vo.OrderItemVO;
import com.example.wechatstore.modules.payment.entity.PaymentRecord;
import com.example.wechatstore.modules.payment.mapper.PaymentRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminOrderService {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;
    private static final DateTimeFormatter NORMAL_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final OrderInfoMapper orderInfoMapper;
    private final OrderItemMapper orderItemMapper;
    private final PaymentRecordMapper paymentRecordMapper;

    public AdminOrderService(
            OrderInfoMapper orderInfoMapper,
            OrderItemMapper orderItemMapper,
            PaymentRecordMapper paymentRecordMapper
    ) {
        this.orderInfoMapper = orderInfoMapper;
        this.orderItemMapper = orderItemMapper;
        this.paymentRecordMapper = paymentRecordMapper;
    }

    public Page<AdminOrderListVO> listOrders(AdminOrderQueryDTO queryDTO) {
        AdminOrderQueryDTO query = queryDTO == null ? new AdminOrderQueryDTO() : queryDTO;
        long current = normalizePage(query.getPage());
        long size = normalizeSize(query.getSize());
        Long total = orderInfoMapper.selectCount(buildListQuery(query, false));
        List<OrderInfo> orders = total == 0
                ? List.of()
                : orderInfoMapper.selectList(buildListQuery(query, true)
                .last("limit " + size + " offset " + ((current - 1) * size)));

        Map<Long, List<OrderItem>> itemsByOrderId = listItemsByOrderIds(orders);
        List<AdminOrderListVO> records = orders.stream()
                .map(order -> toListVO(order, itemsByOrderId.getOrDefault(order.getId(), List.of())))
                .toList();

        Page<AdminOrderListVO> result = new Page<>(current, size, total);
        result.setRecords(records);
        return result;
    }

    public AdminOrderDetailVO getOrderDetail(String orderNo) {
        OrderInfo order = requireOrder(orderNo);
        return toDetailVO(order, listItems(order.getId()), getPayment(order.getOrderNo()));
    }

    @Transactional(rollbackFor = Exception.class)
    public void markDelivering(String orderNo, String trackingNo) {
        OrderInfo order = requireOrder(orderNo);
        if (!OrderStatus.PAID.equals(order.getStatus())) {
            throw new BizException("当前订单状态不允许标记配送中: " + order.getStatus());
        }
        updateStatus(
                orderNo,
                OrderStatus.PAID,
                OrderStatus.DELIVERING,
                LocalDateTime.now(),
                null,
                normalizeTrackingNo(trackingNo)
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public void markFinished(String orderNo) {
        OrderInfo order = requireOrder(orderNo);
        if (!OrderStatus.DELIVERING.equals(order.getStatus())) {
            throw new BizException("当前订单状态不允许标记完成: " + order.getStatus());
        }
        updateStatus(orderNo, OrderStatus.DELIVERING, OrderStatus.FINISHED, null, LocalDateTime.now(), null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String orderNo) {
        OrderInfo order = requireOrder(orderNo);
        if (OrderStatus.PAID.equals(order.getStatus())) {
            throw new BizException("已支付订单暂不支持直接取消，请人工处理退款后再修改状态。");
        }
        if (!OrderStatus.CREATED.equals(order.getStatus())) {
            throw new BizException("当前订单状态不允许取消: " + order.getStatus());
        }
        updateStatus(orderNo, OrderStatus.CREATED, OrderStatus.CANCELLED, null, null, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(String orderNo, String targetStatus) {
        if (OrderStatus.CANCELLED.equals(targetStatus)) {
            cancelOrder(orderNo);
            return;
        }
        if (OrderStatus.DELIVERING.equals(targetStatus)) {
            markDelivering(orderNo, null);
            return;
        }
        if (OrderStatus.FINISHED.equals(targetStatus)) {
            markFinished(orderNo);
            return;
        }
        throw new BizException("不支持将订单状态修改为: " + targetStatus);
    }

    private LambdaQueryWrapper<OrderInfo> buildListQuery(AdminOrderQueryDTO query, boolean withOrder) {
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getOrderNo())) {
            wrapper.like(OrderInfo::getOrderNo, query.getOrderNo().trim());
        }
        if (StringUtils.hasText(query.getPhone())) {
            wrapper.like(OrderInfo::getPhone, query.getPhone().trim());
        }
        if (StringUtils.hasText(query.getReceiverName())) {
            wrapper.like(OrderInfo::getReceiverName, query.getReceiverName().trim());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(OrderInfo::getStatus, query.getStatus().trim());
        }
        LocalDateTime startTime = parseTime(query.getStartTime(), "startTime");
        LocalDateTime endTime = parseTime(query.getEndTime(), "endTime");
        if (startTime != null) {
            wrapper.ge(OrderInfo::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(OrderInfo::getCreateTime, endTime);
        }
        if (withOrder) {
            wrapper.orderByDesc(OrderInfo::getCreateTime)
                    .orderByDesc(OrderInfo::getId);
        }
        return wrapper;
    }

    private OrderInfo requireOrder(String orderNo) {
        if (!StringUtils.hasText(orderNo)) {
            throw new BizException("orderNo is required");
        }
        OrderInfo order = orderInfoMapper.selectOne(new LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getOrderNo, orderNo)
                .last("limit 1"));
        if (order == null) {
            throw new BizException(404, "order not found");
        }
        return order;
    }

    private void updateStatus(
            String orderNo,
            String expectedStatus,
            String targetStatus,
            LocalDateTime deliverTime,
            LocalDateTime finishTime,
            String trackingNo
    ) {
        LambdaUpdateWrapper<OrderInfo> wrapper = new LambdaUpdateWrapper<OrderInfo>()
                .eq(OrderInfo::getOrderNo, orderNo)
                .eq(OrderInfo::getStatus, expectedStatus)
                .set(OrderInfo::getStatus, targetStatus);
        if (deliverTime != null) {
            wrapper.set(OrderInfo::getDeliverTime, deliverTime);
            if (StringUtils.hasText(trackingNo)) {
                wrapper.set(OrderInfo::getTrackingCompany, "顺丰")
                        .set(OrderInfo::getTrackingNo, trackingNo);
            } else {
                wrapper.set(OrderInfo::getTrackingCompany, null)
                        .set(OrderInfo::getTrackingNo, null);
            }
        }
        if (finishTime != null) {
            wrapper.set(OrderInfo::getFinishTime, finishTime);
        }
        int rows = orderInfoMapper.update(null, wrapper);
        if (rows == 0) {
            throw new BizException("订单状态已变更，请刷新后重试。");
        }
    }

    private Map<Long, List<OrderItem>> listItemsByOrderIds(List<OrderInfo> orders) {
        if (orders == null || orders.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> orderIds = orders.stream().map(OrderInfo::getId).toList();
        return orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                        .in(OrderItem::getOrderId, orderIds)
                        .orderByAsc(OrderItem::getId))
                .stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));
    }

    private List<OrderItem> listItems(Long orderId) {
        return orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, orderId)
                .orderByAsc(OrderItem::getId));
    }

    private PaymentRecord getPayment(String orderNo) {
        return paymentRecordMapper.selectOne(new LambdaQueryWrapper<PaymentRecord>()
                .eq(PaymentRecord::getOrderNo, orderNo)
                .last("limit 1"));
    }

    private AdminOrderListVO toListVO(OrderInfo order, List<OrderItem> items) {
        return new AdminOrderListVO(
                order.getOrderNo(),
                order.getStatus(),
                orderStatusText(order.getStatus()),
                order.getTotalAmount(),
                order.getPayAmount(),
                order.getReceiverName(),
                order.getPhone(),
                order.getSchool(),
                order.getProvince(),
                order.getCity(),
                order.getDistrict(),
                order.getDetailAddress(),
                buildItemSummary(items),
                order.getTrackingCompany(),
                order.getTrackingNo(),
                order.getCreateTime(),
                order.getPayTime(),
                order.getDeliverTime(),
                order.getFinishTime()
        );
    }

    private AdminOrderDetailVO toDetailVO(OrderInfo order, List<OrderItem> items, PaymentRecord paymentRecord) {
        return new AdminOrderDetailVO(
                order.getOrderNo(),
                order.getStatus(),
                orderStatusText(order.getStatus()),
                order.getPayStatus(),
                paymentStatusText(order.getPayStatus()),
                order.getTotalAmount(),
                order.getPayAmount(),
                order.getReceiverName(),
                order.getPhone(),
                order.getSchool(),
                order.getProvince(),
                order.getCity(),
                order.getDistrict(),
                order.getDetailAddress(),
                order.getRemark(),
                order.getTrackingCompany(),
                order.getTrackingNo(),
                items.stream().map(this::toItemVO).toList(),
                toPaymentVO(paymentRecord),
                order.getCreateTime(),
                order.getPayTime(),
                order.getDeliverTime(),
                order.getFinishTime()
        );
    }

    private OrderItemVO toItemVO(OrderItem item) {
        return new OrderItemVO(
                item.getBookId(),
                item.getBookName(),
                item.getCoverUrl(),
                item.getPrice(),
                item.getQuantity(),
                item.getSubtotal()
        );
    }

    private AdminPaymentVO toPaymentVO(PaymentRecord paymentRecord) {
        if (paymentRecord == null) {
            return null;
        }
        return new AdminPaymentVO(
                paymentRecord.getTransactionId(),
                paymentRecord.getAmount(),
                paymentRecord.getPayStatus(),
                paymentStatusText(paymentRecord.getPayStatus()),
                paymentRecord.getPayTime()
        );
    }

    private String orderStatusText(String status) {
        if (OrderStatus.CREATED.equals(status)) {
            return "待支付";
        }
        if (OrderStatus.PAID.equals(status)) {
            return "待发货";
        }
        if (OrderStatus.DELIVERING.equals(status)) {
            return "配送中";
        }
        if (OrderStatus.FINISHED.equals(status)) {
            return "已完成";
        }
        if (OrderStatus.CANCELLED.equals(status)) {
            return "已取消";
        }
        return "未知状态";
    }

    private String paymentStatusText(Integer status) {
        if (status == null) {
            return "未知状态";
        }
        if (PaymentStatus.PENDING == status) {
            return "未支付";
        }
        if (PaymentStatus.SUCCESS == status) {
            return "已支付";
        }
        if (PaymentStatus.CLOSED == status) {
            return "已关闭";
        }
        return "未知状态";
    }

    private String buildItemSummary(List<OrderItem> items) {
        return items.stream()
                .map(item -> item.getBookName() + " x" + item.getQuantity())
                .collect(Collectors.joining(", "));
    }

    private String normalizeTrackingNo(String trackingNo) {
        if (!StringUtils.hasText(trackingNo)) {
            return null;
        }
        return trackingNo.trim();
    }

    private long normalizePage(Integer page) {
        return page == null || page < 1 ? DEFAULT_PAGE : page;
    }

    private long normalizeSize(Integer size) {
        if (size == null || size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }

    private LocalDateTime parseTime(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        try {
            return LocalDateTime.parse(trimmed, NORMAL_DATE_TIME);
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDateTime.parse(trimmed, DateTimeFormatter.ISO_DATE_TIME);
            } catch (DateTimeParseException ex) {
                throw new BizException(fieldName + " format must be yyyy-MM-dd HH:mm:ss or ISO date-time");
            }
        }
    }
}
