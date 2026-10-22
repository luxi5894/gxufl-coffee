package com.gxufl.coffee.controller;

import com.gxufl.coffee.common.PageResult;
import com.gxufl.coffee.common.Result;
import com.gxufl.coffee.dto.CatalogDtos;
import com.gxufl.coffee.entity.Category;
import com.gxufl.coffee.service.CategoryService;
import com.gxufl.coffee.service.ProductService;
import com.gxufl.coffee.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CatalogController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ReviewService reviewService;

    @GetMapping("/categories")
    public Result<List<Category>> categories() {
        return Result.ok(categoryService.listPublic());
    }

    @GetMapping("/products")
    public Result<PageResult<CatalogDtos.ProductVO>> products(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean recommended,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(productService.listPublic(keyword, categoryId, recommended, page, pageSize));
    }

    @GetMapping("/products/recommended")
    public Result<List<CatalogDtos.ProductVO>> recommended() {
        return Result.ok(productService.recommended());
    }

    @GetMapping("/products/{id}")
    public Result<CatalogDtos.ProductVO> detail(@PathVariable Long id) {
        return Result.ok(productService.detail(id));
    }

    @GetMapping("/products/{id}/reviews")
    public Result<PageResult<CatalogDtos.ReviewVO>> reviews(@PathVariable Long id,
                                                           @RequestParam(defaultValue = "1") Integer page,
                                                           @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.ok(reviewService.listByProduct(id, page, pageSize));
    }
}
