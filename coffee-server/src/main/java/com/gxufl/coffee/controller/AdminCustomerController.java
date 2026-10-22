package com.gxufl.coffee.controller;

import com.gxufl.coffee.common.PageResult;
import com.gxufl.coffee.common.Result;
import com.gxufl.coffee.dto.AuthDtos;
import com.gxufl.coffee.dto.CatalogDtos;
import com.gxufl.coffee.dto.StatsDtos;
import com.gxufl.coffee.service.OrderService;
import com.gxufl.coffee.service.ReviewService;
import com.gxufl.coffee.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminCustomerController {

    private final UserService userService;
    private final ReviewService reviewService;
    private final OrderService orderService;

    @GetMapping("/stats")
    public Result<StatsDtos.StatsVO> stats() {
        return Result.ok(orderService.stats());
    }

    @GetMapping("/users")
    public Result<PageResult<AuthDtos.UserVO>> users(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(userService.list(keyword, page, pageSize));
    }

    @PutMapping("/users/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @Valid @RequestBody StatsDtos.StatusRequest request) {
        userService.updateStatus(id, request.getStatus());
        return Result.ok();
    }

    @GetMapping("/reviews")
    public Result<PageResult<CatalogDtos.ReviewVO>> reviews(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(reviewService.adminList(keyword, status, page, pageSize));
    }

    @PutMapping("/reviews/{id}/status")
    public Result<Void> updateReviewStatus(@PathVariable Long id,
                                           @Valid @RequestBody StatsDtos.StatusRequest request) {
        reviewService.updateStatus(id, request.getStatus());
        return Result.ok();
    }

    @DeleteMapping("/reviews/{id}")
    public Result<Void> deleteReview(@PathVariable Long id) {
        reviewService.delete(id);
        return Result.ok();
    }
}
