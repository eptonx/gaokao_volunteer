package com.example.gaokaoproject.dto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private Long userId;
    private String accessToken;
    private String refreshToken;
}