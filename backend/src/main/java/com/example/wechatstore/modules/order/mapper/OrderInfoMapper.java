package com.example.wechatstore.modules.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.wechatstore.modules.admin.vo.AdminWeeklySalesVO;
import com.example.wechatstore.modules.order.entity.OrderInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    @Select("""
            SELECT DATE_FORMAT(COALESCE(o.pay_time, o.create_time), '%Y-%m-%d') AS date,
                   COALESCE(SUM(oi.quantity), 0) AS salesVolume,
                   0 AS salesAmount
            FROM order_info o
            INNER JOIN order_item oi ON oi.order_id = o.id
            WHERE o.status IN ('PAID', 'DELIVERING', 'FINISHED')
              AND COALESCE(o.pay_time, o.create_time) >= #{startTime}
              AND COALESCE(o.pay_time, o.create_time) < #{endTime}
            GROUP BY DATE_FORMAT(COALESCE(o.pay_time, o.create_time), '%Y-%m-%d')
            ORDER BY date ASC
            """)
    List<AdminWeeklySalesVO> selectWeeklySalesVolume(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Select("""
            SELECT DATE_FORMAT(COALESCE(pay_time, create_time), '%Y-%m-%d') AS date,
                   0 AS salesVolume,
                   COALESCE(SUM(COALESCE(pay_amount, total_amount, 0)), 0) AS salesAmount
            FROM order_info
            WHERE status IN ('PAID', 'DELIVERING', 'FINISHED')
              AND COALESCE(pay_time, create_time) >= #{startTime}
              AND COALESCE(pay_time, create_time) < #{endTime}
            GROUP BY DATE_FORMAT(COALESCE(pay_time, create_time), '%Y-%m-%d')
            ORDER BY date ASC
            """)
    List<AdminWeeklySalesVO> selectWeeklySalesAmount(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}
