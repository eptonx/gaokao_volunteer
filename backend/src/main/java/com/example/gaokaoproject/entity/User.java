package com.example.gaokaoproject.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String phone;
    private String passwordHash;
    private String nickname;
    private String avatarUrl;
    private String wechatOpenid;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}