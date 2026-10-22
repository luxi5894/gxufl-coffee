package com.gxufl.coffee.controller;

import com.gxufl.coffee.common.PageResult;
import com.gxufl.coffee.common.Result;
import com.gxufl.coffee.dto.OrderDtos;
import com.gxufl.coffee.security.UserContext;
import com.gxufl.coffee.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Result<OrderDtos.OrderVO> create(@Valid @RequestBody OrderDtos.Create request) {
        return Result.ok(orderService.create(UserContext.userId(), request));
    }

    @GetMapping
    public Result<PageResult<OrderDtos.OrderVO>> list(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(orderService.list(UserContext.userId(), status, page, pageSize));
    }

    @GetMapping("/{id}")
    public Result<OrderDtos.OrderVO> detail(@PathVariable Long id) {
        return Result.ok(orderService.detail(UserContext.userId(), id, false));
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        orderService.cancel(UserContext.userId(), id);
        return Result.ok();
    }

    @PostMapping("/{id}/confirm")
    public Result<Void> confirm(@PathVariable Long id) {
        orderService.confirm(UserContext.userId(), id);
        return Result.ok();
    }
}
