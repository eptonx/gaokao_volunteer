package com.example.gaokaoproject.entity;
import lombok.Data;

@Data
public class InstitutionUser {
    private Long id;
    private Long institutionId;
    private String username;
    private String passwordHash;
    private String realName;
    private String mobile;
    private String email;
    private java.time.LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private Integer status;
    private java.time.LocalDateTime createdAt;
}