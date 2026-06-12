package com.example.wechatstore.modules.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.wechatstore.common.enums.OrderStatus;
import com.example.wechatstore.common.enums.PaymentStatus;
import com.example.wechatstore.common.exception.BizException;
import com.example.wechatstore.common.exception.UnauthorizedException;
import com.example.wechatstore.modules.book.entity.Book;
import com.example.wechatstore.modules.book.mapper.BookMapper;
import com.example.wechatstore.modules.order.dto.CreatePayOrderDTO;
import com.example.wechatstore.modules.order.dto.OrderBookDTO;
import com.example.wechatstore.modules.order.entity.OrderInfo;
import com.example.wechatstore.modules.order.entity.OrderItem;
import com.example.wechatstore.modules.order.mapper.OrderInfoMapper;
import com.example.wechatstore.modules.order.mapper.OrderItemMapper;
import com.example.wechatstore.modules.order.vo.CreatedOrderVO;
import com.example.wechatstore.modules.order.vo.OrderDetailVO;
import com.example.wechatstore.modules.order.vo.OrderItemVO;
import com.example.wechatstore.modules.payment.entity.PaymentRecord;
import com.example.wechatstore.modules.payment.mapper.PaymentRecordMapper;
import com.example.wechatstore.modules.user.entity.WxUser;
import com.example.wechatstore.modules.user.mapper.WxUserMapper;
import com.example.wechatstore.utils.OrderNoGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final List<String> UNFINISHED_STATUSES = List.of(
            OrderStatus.CREATED,
            OrderStatus.PAID,
            OrderStatus.DELIVERING
    );

    private final BookMapper bookMapper;
    private final WxUserMapper wxUserMapper;
    private final OrderInfoMapper orderInfoMapper;
    private final OrderItemMapper orderItemMapper;
    private final PaymentRecordMapper paymentRecordMapper;
    private final OrderNoGenerator orderNoGenerator;

    public OrderService(
            BookMapper bookMapper,
            WxUserMapper wxUserMapper,
            OrderInfoMapper orderInfoMapper,
            OrderItemMapper orderItemMapper,
            PaymentRecordMapper paymentRecordMapper,
            OrderNoGenerator orderNoGenerator
    ) {
        this.bookMapper = bookMapper;
        this.wxUserMapper = wxUserMapper;
        this.orderInfoMapper = orderInfoMapper;
        this.orderItemMapper = orderItemMapper;
        this.paymentRecordMapper = paymentRecordMapper;
        this.orderNoGenerator = orderNoGenerator;
    }

    @Transactional(rollbackFor = Exception.class)
    public CreatedOrderVO createOrder(Long userId, String openid, CreatePayOrderDTO dto) {
        requireUser(userId, openid);

        Map<Long, Integer> quantityByBookId = mergeItems(dto.items());
        List<Book> books = bookMapper.selectList(new LambdaQueryWrapper<Book>()
                .in(Book::getId, quantityByBookId.keySet())
                .eq(Book::getStatus, 1)
                .eq(Book::getDeleted, 0));
        if (books.size() != quantityByBookId.size()) {
            throw new BizException("book not found or off shelf");
        }

        Map<Long, Book> bookById = books.stream()
                .collect(Collectors.toMap(Book::getId, Function.identity()));
        validateStock(quantityByBookId, bookById);

        BigDecimal totalAmount = quantityByBookId.entrySet().stream()
                .map(entry -> {
                    Book book = bookById.get(entry.getKey());
                    return book.getPrice().multiply(BigDecimal.valueOf(entry.getValue()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        OrderInfo order = buildOrder(userId, dto, totalAmount);
        orderInfoMapper.insert(order);

        for (Map.Entry<Long, Integer> entry : quantityByBookId.entrySet()) {
            Book book = bookById.get(entry.getKey());
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setBookId(book.getId());
            item.setBookName(book.getName());
            item.setCoverUrl(book.getCoverUrl());
            item.setPrice(book.getPrice());
            item.setQuantity(entry.getValue());
            item.setSubtotal(book.getPrice().multiply(BigDecimal.valueOf(entry.getValue())));
            orderItemMapper.insert(item);
        }

        PaymentRecord paymentRecord = new PaymentRecord();
        paymentRecord.setOrderId(order.getId());
        paymentRecord.setOrderNo(order.getOrderNo());
        paymentRecord.setAmount(order.getPayAmount());
        paymentRecord.setPayStatus(PaymentStatus.PENDING);
        paymentRecord.setPayChannel("JSAPI");
        paymentRecordMapper.insert(paymentRecord);

        return new CreatedOrderVO(order);
    }

    public OrderDetailVO getOrderDetail(Long userId, String orderNo) {
        OrderInfo order = orderInfoMapper.selectOne(new LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getUserId, userId)
                .eq(OrderInfo::getOrderNo, orderNo)
                .last("limit 1"));
        if (order == null) {
            throw new BizException(404, "order not found");
        }
        return toDetailVO(order);
    }

    public List<OrderDetailVO> listMyOrders(Long userId, boolean unfinished) {
        LambdaQueryWrapper<OrderInfo> query = new LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getUserId, userId);
        if (unfinished) {
            query.in(OrderInfo::getStatus, UNFINISHED_STATUSES);
        }
        query.orderByDesc(OrderInfo::getCreateTime)
                .orderByDesc(OrderInfo::getId);

        return orderInfoMapper.selectList(query)
                .stream()
                .map(this::toDetailVO)
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelMyOrder(Long userId, String orderNo) {
        int rows = orderInfoMapper.update(null, new LambdaUpdateWrapper<OrderInfo>()
                .eq(OrderInfo::getUserId, userId)
                .eq(OrderInfo::getOrderNo, orderNo)
                .eq(OrderInfo::getStatus, OrderStatus.CREATED)
                .set(OrderInfo::getStatus, OrderStatus.CANCELLED)
                .set(OrderInfo::getPayStatus, PaymentStatus.CLOSED));
        if (rows == 0) {
            throw new BizException("订单不存在或当前状态不允许取消");
        }
    }

    private WxUser requireUser(Long userId, String openid) {
        WxUser user = wxUserMapper.selectById(userId);
        if (user == null || !Objects.equals(user.getOpenid(), openid)) {
            throw new UnauthorizedException("h5 login required");
        }
        return user;
    }

    private Map<Long, Integer> mergeItems(List<OrderBookDTO> items) {
        if (items == null || items.isEmpty()) {
            throw new BizException("items are required");
        }
        Map<Long, Integer> merged = new LinkedHashMap<>();
        for (OrderBookDTO item : items) {
            if (item.bookId() == null || item.quantity() == null || item.quantity() < 1) {
                throw new BizException("invalid order item");
            }
            merged.merge(item.bookId(), item.quantity(), Integer::sum);
        }
        return merged;
    }

    private void validateStock(Map<Long, Integer> quantityByBookId, Map<Long, Book> bookById) {
        for (Map.Entry<Long, Integer> entry : quantityByBookId.entrySet()) {
            Book book = bookById.get(entry.getKey());
            int stock = book.getStock() == null ? 0 : book.getStock();
            if (stock < entry.getValue()) {
                throw new BizException("book stock not enough: " + book.getName());
            }
        }
    }

    private OrderInfo buildOrder(Long userId, CreatePayOrderDTO dto, BigDecimal totalAmount) {
        OrderInfo order = new OrderInfo();
        order.setOrderNo(orderNoGenerator.nextOrderNo());
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setPayAmount(totalAmount);
        order.setStatus(OrderStatus.CREATED);
        order.setPayStatus(PaymentStatus.PENDING);
        order.setReceiverName(dto.receiverName());
        order.setPhone(dto.phone());
        order.setSchool(dto.school());
        order.setProvince(dto.province());
        order.setCity(dto.city());
        order.setDistrict(dto.district());
        order.setDetailAddress(dto.detailAddress());
        order.setRemark(dto.remark());
        return order;
    }

    private OrderDetailVO toDetailVO(OrderInfo order) {
        return new OrderDetailVO(
                order.getOrderNo(),
                order.getStatus(),
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
                listItems(order.getId()),
                order.getPayTime(),
                order.getCreateTime(),
                order.getDeliverTime(),
                order.getFinishTime()
        );
    }

    private List<OrderItemVO> listItems(Long orderId) {
        return orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, orderId)
                        .orderByAsc(OrderItem::getId))
                .stream()
                .map(item -> new OrderItemVO(
                        item.getBookId(),
                        item.getBookName(),
                        item.getCoverUrl(),
                        item.getPrice(),
                        item.getQuantity(),
                        item.getSubtotal()
                ))
                .toList();
    }
}
