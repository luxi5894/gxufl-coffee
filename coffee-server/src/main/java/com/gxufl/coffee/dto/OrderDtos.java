package com.gxufl.coffee.dto;

import com.gxufl.coffee.entity.OrderItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class OrderDtos {

    private OrderDtos() {
    }

    @Data
    public static class Create {
        private List<Long> cartItemIds;

        @Valid
        private List<Item> items;

        @Size(max = 100, message = "备注最多100个字符")
        private String remark;
    }

    @Data
    public static class Item {
        @NotNull(message = "商品不能为空")
        private Long productId;
        private Long specId;

        @NotNull(message = "数量不能为空")
        @Min(value = 1, message = "数量至少为1")
        @Max(value = 99, message = "单件商品最多99份")
        private Integer quantity;
    }

    @Data
    public static class OrderVO {
        private Long id;
        private String orderNo;
        private Long userId;
        private String userNickname;
        private String userPhone;
        private BigDecimal totalAmount;
        private Integer itemCount;
        private Integer status;
        private String remark;
        private String pickupCode;
        private LocalDateTime createdAt;
        private List<OrderItem> items;
    }
}
