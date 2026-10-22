package com.gxufl.coffee.controller;

import com.gxufl.coffee.common.Result;
import com.gxufl.coffee.dto.CartDtos;
import com.gxufl.coffee.security.UserContext;
import com.gxufl.coffee.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public Result<List<CartDtos.CartVO>> list() {
        return Result.ok(cartService.list(UserContext.userId()));
    }

    @PostMapping
    public Result<Void> add(@Valid @RequestBody CartDtos.Add request) {
        cartService.add(UserContext.userId(), request);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CartDtos.Update request) {
        cartService.update(UserContext.userId(), id, request);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        cartService.delete(UserContext.userId(), id);
        return Result.ok();
    }

    @DeleteMapping
    public Result<Void> clear() {
        cartService.clear(UserContext.userId());
        return Result.ok();
    }

    @GetMapping("/count")
    public Result<Map<String, Object>> count() {
        return Result.ok(cartService.count(UserContext.userId()));
    }
}
