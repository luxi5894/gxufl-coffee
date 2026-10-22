package com.gxufl.coffee.controller;

import com.gxufl.coffee.common.Result;
import com.gxufl.coffee.dto.AuthDtos;
import com.gxufl.coffee.security.UserContext;
import com.gxufl.coffee.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Result<AuthDtos.LoginVO> register(@Valid @RequestBody AuthDtos.Register request) {
        return Result.ok(authService.register(request));
    }

    @PostMapping("/login")
    public Result<AuthDtos.LoginVO> login(@Valid @RequestBody AuthDtos.Login request) {
        return Result.ok(authService.login(request));
    }

    @PostMapping("/admin/login")
    public Result<AuthDtos.LoginVO> adminLogin(@Valid @RequestBody AuthDtos.AdminLogin request) {
        return Result.ok(authService.adminLogin(request));
    }

    @GetMapping("/me")
    public Result<AuthDtos.UserVO> me() {
        return Result.ok(authService.me(UserContext.userId()));
    }

    @PutMapping("/me")
    public Result<AuthDtos.UserVO> updateProfile(@Valid @RequestBody AuthDtos.Profile request) {
        return Result.ok(authService.updateProfile(UserContext.userId(), request));
    }
}
