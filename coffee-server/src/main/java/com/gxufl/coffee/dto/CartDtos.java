package com.gxufl.coffee.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class CartDtos {

    private CartDtos() {
    }

    @Data
    public static class Add {
        @NotNull(message = "请选择商品")
        private Long productId;
        private Long specId;

        @NotNull(message = "请选择数量")
        @Min(value = 1, message = "数量至少为1")
        @Max(value = 99, message = "单件商品最多99份")
        private Integer quantity;
    }

    @Data
    public static class Update {
        @NotNull(message = "请填写数量")
        @Min(value = 1, message = "数量至少为1")
        @Max(value = 99, message = "单件商品最多99份")
        private Integer quantity;
    }

    @Data
    public static class CartVO {
        private Long id;
        private Long productId;
        private String productName;
        private String productImage;
        private Long specId;
        private String specName;
        private BigDecimal price;
        private Integer quantity;
        private Integer stock;
        private Integer productStatus;
        private LocalDateTime createdAt;
    }
}
