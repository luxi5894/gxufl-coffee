package com.gxufl.coffee.controller;

import com.gxufl.coffee.common.PageResult;
import com.gxufl.coffee.common.Result;
import com.gxufl.coffee.dto.CatalogDtos;
import com.gxufl.coffee.dto.StatsDtos;
import com.gxufl.coffee.entity.Category;
import com.gxufl.coffee.service.CategoryService;
import com.gxufl.coffee.service.ProductService;
import com.gxufl.coffee.service.UploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminCatalogController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final UploadService uploadService;

    @GetMapping("/products")
    public Result<PageResult<CatalogDtos.ProductVO>> products(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(productService.listAdmin(keyword, categoryId, page, pageSize));
    }

    @GetMapping("/products/{id}")
    public Result<CatalogDtos.ProductVO> product(@PathVariable Long id) {
        return Result.ok(productService.adminDetail(id));
    }

    @PostMapping("/products")
    public Result<CatalogDtos.ProductVO> createProduct(@Valid @RequestBody CatalogDtos.ProductRequest request) {
        return Result.ok(productService.create(request));
    }

    @PutMapping("/products/{id}")
    public Result<CatalogDtos.ProductVO> updateProduct(@PathVariable Long id,
                                                       @Valid @RequestBody CatalogDtos.ProductRequest request) {
        return Result.ok(productService.update(id, request));
    }

    @DeleteMapping("/products/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return Result.ok();
    }

    @PutMapping("/products/{id}/status")
    public Result<Void> updateProductStatus(@PathVariable Long id, @Valid @RequestBody StatsDtos.StatusRequest request) {
        productService.updateStatus(id, request.getStatus());
        return Result.ok();
    }

    @PutMapping("/products/{id}/recommend")
    public Result<Void> updateRecommended(@PathVariable Long id, @Valid @RequestBody StatsDtos.StatusRequest request) {
        productService.updateRecommended(id, request.getStatus());
        return Result.ok();
    }

    @GetMapping("/categories")
    public Result<List<Category>> categories() {
        return Result.ok(categoryService.listAll());
    }

    @PostMapping("/categories")
    public Result<Category> createCategory(@Valid @RequestBody CatalogDtos.CategoryRequest request) {
        return Result.ok(categoryService.create(request));
    }

    @PutMapping("/categories/{id}")
    public Result<Category> updateCategory(@PathVariable Long id,
                                           @Valid @RequestBody CatalogDtos.CategoryRequest request) {
        return Result.ok(categoryService.update(id, request));
    }

    @DeleteMapping("/categories/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.ok();
    }

    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        return Result.ok(Map.of("url", uploadService.store(file)));
    }
}
