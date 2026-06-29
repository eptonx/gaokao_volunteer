package com.example.gaokaoproject.controller;

import com.example.gaokaoproject.dto.AuthResponse;
import com.example.gaokaoproject.dto.LoginRequest;
import com.example.gaokaoproject.dto.RegisterRequest;
import com.example.gaokaoproject.res.Result;
import com.example.gaokaoproject.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public Result<Long> register(@Valid @RequestBody RegisterRequest request) {
        Long userId = authService.register(request);
        return Result.ok(userId);
    }

    @PostMapping("/login")
    public Result<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse resp = authService.login(request);
        return Result.ok(resp);
    }
}
