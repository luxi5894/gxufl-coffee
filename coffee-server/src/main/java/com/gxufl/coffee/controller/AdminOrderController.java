package com.gxufl.coffee.controller;

import com.gxufl.coffee.common.PageResult;
import com.gxufl.coffee.common.Result;
import com.gxufl.coffee.dto.OrderDtos;
import com.gxufl.coffee.dto.StatsDtos;
import com.gxufl.coffee.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping("/orders")
    public Result<PageResult<OrderDtos.OrderVO>> orders(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(orderService.adminList(status, keyword, page, pageSize));
    }

    @GetMapping("/orders/{id}")
    public Result<OrderDtos.OrderVO> order(@PathVariable Long id) {
        return Result.ok(orderService.detail(null, id, true));
    }

    @PutMapping("/orders/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatsDtos.StatusRequest request) {
        orderService.adminUpdateStatus(id, request.getStatus());
        return Result.ok();
    }
}
