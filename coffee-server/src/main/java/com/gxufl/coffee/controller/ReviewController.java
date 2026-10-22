package com.gxufl.coffee.controller;

import com.gxufl.coffee.common.PageResult;
import com.gxufl.coffee.common.Result;
import com.gxufl.coffee.dto.CatalogDtos;
import com.gxufl.coffee.security.UserContext;
import com.gxufl.coffee.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public Result<CatalogDtos.ReviewVO> create(@Valid @RequestBody CatalogDtos.ReviewCreate request) {
        return Result.ok(reviewService.create(UserContext.userId(), request));
    }

    @GetMapping("/mine")
    public Result<PageResult<CatalogDtos.ReviewVO>> mine(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(reviewService.mine(UserContext.userId(), page, pageSize));
    }
}
