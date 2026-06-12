package com.example.wechatstore.modules.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.wechatstore.common.enums.OrderStatus;
import com.example.wechatstore.modules.admin.vo.AdminDashboardOverviewVO;
import com.example.wechatstore.modules.admin.vo.AdminWeeklySalesVO;
import com.example.wechatstore.modules.book.entity.Book;
import com.example.wechatstore.modules.book.mapper.BookMapper;
import com.example.wechatstore.modules.order.entity.OrderInfo;
import com.example.wechatstore.modules.order.mapper.OrderInfoMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminDashboardService {

    private final OrderInfoMapper orderInfoMapper;
    private final BookMapper bookMapper;

    public AdminDashboardService(OrderInfoMapper orderInfoMapper, BookMapper bookMapper) {
        this.orderInfoMapper = orderInfoMapper;
        this.bookMapper = bookMapper;
    }

    public AdminDashboardOverviewVO getOverview() {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();
        LocalDate startDate = today.minusDays(6);
        LocalDateTime weekStart = startDate.atStartOfDay();

        Long todayOrderCount = orderInfoMapper.selectCount(new LambdaQueryWrapper<OrderInfo>()
                .ge(OrderInfo::getCreateTime, todayStart)
                .lt(OrderInfo::getCreateTime, tomorrowStart)
                .ne(OrderInfo::getStatus, OrderStatus.CANCELLED));
        Long pendingDeliveryCount = orderInfoMapper.selectCount(new LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getStatus, OrderStatus.PAID));
        Long onSaleBookCount = bookMapper.selectCount(new LambdaQueryWrapper<Book>()
                .eq(Book::getStatus, 1)
                .eq(Book::getDeleted, 0));

        return new AdminDashboardOverviewVO(
                todayOrderCount,
                pendingDeliveryCount,
                onSaleBookCount,
                buildWeeklySales(startDate, weekStart, tomorrowStart)
        );
    }

    private List<AdminWeeklySalesVO> buildWeeklySales(
            LocalDate startDate,
            LocalDateTime weekStart,
            LocalDateTime endTime
    ) {
        Map<String, AdminWeeklySalesVO> weeklySalesByDate = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            AdminWeeklySalesVO item = new AdminWeeklySalesVO();
            item.setDate(date.toString());
            item.setSalesVolume(0L);
            item.setSalesAmount(BigDecimal.ZERO);
            weeklySalesByDate.put(item.getDate(), item);
        }

        /*
         * Paid-order statistics are attributed by pay_time first. Local test data may not
         * always have pay_time populated, so the mapper SQL falls back to create_time.
         */
        mergeVolume(weeklySalesByDate, orderInfoMapper.selectWeeklySalesVolume(weekStart, endTime));
        mergeAmount(weeklySalesByDate, orderInfoMapper.selectWeeklySalesAmount(weekStart, endTime));
        return weeklySalesByDate.values().stream().toList();
    }

    private void mergeVolume(Map<String, AdminWeeklySalesVO> target, List<AdminWeeklySalesVO> source) {
        for (AdminWeeklySalesVO item : source) {
            AdminWeeklySalesVO targetItem = target.get(item.getDate());
            if (targetItem != null && item.getSalesVolume() != null) {
                targetItem.setSalesVolume(item.getSalesVolume());
            }
        }
    }

    private void mergeAmount(Map<String, AdminWeeklySalesVO> target, List<AdminWeeklySalesVO> source) {
        for (AdminWeeklySalesVO item : source) {
            AdminWeeklySalesVO targetItem = target.get(item.getDate());
            if (targetItem != null && item.getSalesAmount() != null) {
                targetItem.setSalesAmount(item.getSalesAmount());
            }
        }
    }
}
