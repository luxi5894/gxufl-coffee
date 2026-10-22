package com.gxufl.coffee.controller;

import com.gxufl.coffee.common.Result;
import com.gxufl.coffee.dto.StatsDtos;
import com.gxufl.coffee.security.UserContext;
import com.gxufl.coffee.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final OrderService orderService;

    @GetMapping("/me/stats")
    public Result<StatsDtos.UserStatsVO> stats() {
        return Result.ok(orderService.userStats(UserContext.userId()));
    }
}
