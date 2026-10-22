package com.gxufl.coffee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gxufl.coffee.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface StatsMapper extends BaseMapper<Order> {

    @Select("SELECT COUNT(*) FROM orders WHERE status <> 4 AND created_at >= #{start}")
    long countOrders(@Param("start") LocalDateTime start);

    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE status <> 4 AND created_at >= #{start}")
    BigDecimal revenueSince(@Param("start") LocalDateTime start);

    @Select("SELECT COUNT(*) FROM sys_user WHERE role = 'USER' AND created_at >= #{start}")
    long countNewUsers(@Param("start") LocalDateTime start);

    @Select("SELECT DATE(created_at) AS day, COUNT(*) AS orders, COALESCE(SUM(total_amount), 0) AS revenue " +
            "FROM orders WHERE status <> 4 AND created_at >= #{start} GROUP BY DATE(created_at) ORDER BY day")
    List<Map<String, Object>> dailyTrend(@Param("start") LocalDateTime start);

    @Select("SELECT oi.product_name AS name, SUM(oi.quantity) AS quantity, SUM(oi.price * oi.quantity) AS amount " +
            "FROM order_item oi JOIN orders o ON o.id = oi.order_id " +
            "WHERE o.status <> 4 AND o.created_at >= #{start} " +
            "GROUP BY oi.product_name ORDER BY quantity DESC LIMIT 5")
    List<Map<String, Object>> topProducts(@Param("start") LocalDateTime start);

    @Select("SELECT status, COUNT(*) AS total FROM orders GROUP BY status")
    List<Map<String, Object>> statusDistribution();

    @Select("SELECT COUNT(*) FROM orders WHERE status IN (0, 1)")
    long countPending();
}
