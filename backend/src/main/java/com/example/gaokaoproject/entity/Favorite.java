package com.example.gaokaoproject.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Favorite {
    private Long id;
    private Long userId;
    private Integer type;
    private Long targetId;
    private Long enrollmentPlanId;
    private LocalDateTime createdAt;
}
