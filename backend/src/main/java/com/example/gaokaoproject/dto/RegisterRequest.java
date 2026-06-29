package com.example.gaokaoproject.dto;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class RegisterRequest {
    @NotBlank private String phone;
    @NotBlank private String password;
    @NotBlank private String nickname;
}