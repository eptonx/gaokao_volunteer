package com.example.gaokaoproject.service;

import com.example.gaokaoproject.dto.AuthResponse;
import com.example.gaokaoproject.dto.LoginRequest;
import com.example.gaokaoproject.dto.RegisterRequest;

public interface AuthService {
    Long register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
