package com.gxufl.coffee.dto;

import com.gxufl.coffee.entity.ProductSpec;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class CatalogDtos {

    private CatalogDtos() {
    }

    @Data
    public static class ProductVO {
        private Long id;
        private Long categoryId;
        private String categoryName;
        private String name;
        private String subtitle;
        private String description;
        private BigDecimal price;
        private BigDecimal originalPrice;
        private String image;
        private List<String> images;
        private List<String> tags;
        private String origin;
        private String process;
        private List<String> temperature;
        private BigDecimal rating;
        private Integer sales;
        private Integer stock;
        private Integer status;
        private Integer recommended;
        private Integer sort;
        private List<ProductSpec> specs;
        private LocalDateTime createdAt;
    }

    @Data
    public static class ProductRequest {
        @NotNull(message = "请选择分类")
        private Long categoryId;

        @NotBlank(message = "请填写商品名称")
        @Size(max = 40, message = "商品名称最多40个字符")
        private String name;

        @Size(max = 60, message = "副标题最多60个字符")
        private String subtitle;

        @Size(max = 500, message = "描述最多500个字符")
        private String description;

        @NotNull(message = "请填写价格")
        @DecimalMin(value = "0.01", message = "价格必须大于0")
        private BigDecimal price;

        private BigDecimal originalPrice;
        private String image;
        private List<String> images;
        private List<String> tags;
        private String origin;
        private String process;
        private List<String> temperature;

        @DecimalMin(value = "0", message = "评分不能为负数")
        private BigDecimal rating;

        @Min(value = 0, message = "库存不能为负数")
        private Integer stock;

        private Integer status;
        private Integer recommended;
        private Integer sort;

        @Valid
        private List<SpecRequest> specs;
    }

    @Data
    public static class SpecRequest {
        private Long id;

        @NotBlank(message = "请填写规格名称")
        @Size(max = 30, message = "规格名称最多30个字符")
        private String name;

        private BigDecimal priceDelta;
        private Integer sort;
    }

    @Data
    public static class CategoryRequest {
        @NotBlank(message = "请填写分类名称")
        @Size(max = 20, message = "分类名称最多20个字符")
        private String name;
        private String nameEn;
        private String icon;
        private Integer sort;
        private Integer status;
    }

    @Data
    public static class ReviewCreate {
        @NotNull(message = "请选择商品")
        private Long productId;

        @NotNull(message = "请选择评分")
        @Min(value = 1, message = "评分为1-5星")
        @Max(value = 5, message = "评分为1-5星")
        private Integer rating;

        @NotBlank(message = "请填写评价内容")
        @Size(max = 200, message = "评价最多200个字符")
        private String content;
    }

    @Data
    public static class ReviewVO {
        private Long id;
        private Long productId;
        private String productName;
        private String productImage;
        private String nickname;
        private Integer rating;
        private String content;
        private Integer status;
        private LocalDateTime createdAt;
    }
}
