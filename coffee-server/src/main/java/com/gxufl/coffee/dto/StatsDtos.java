package com.gxufl.coffee.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

public final class StatsDtos {

    private StatsDtos() {
    }

    @Data
    public static class StatsVO {
        private long todayOrders;
        private BigDecimal todayRevenue;
        private long todayUsers;
        private long totalUsers;
        private long totalOrders;
        private BigDecimal totalRevenue;
        private long pendingOrders;
        private long productCount;
        private List<DailyPoint> trend;
        private List<TopProduct> topProducts;
        private List<StatusCount> statusDistribution;
        private List<OrderDtos.OrderVO> recentOrders;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyPoint {
        private String date;
        private long orders;
        private BigDecimal revenue;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopProduct {
        private String name;
        private long quantity;
        private BigDecimal amount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusCount {
        private int status;
        private long total;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserStatsVO {
        private long orderCount;
        private long completedCount;
        private BigDecimal totalSpent;
    }

    @Data
    public static class StatusRequest {
        @NotNull(message = "状态不能为空")
        private Integer status;
    }
}
