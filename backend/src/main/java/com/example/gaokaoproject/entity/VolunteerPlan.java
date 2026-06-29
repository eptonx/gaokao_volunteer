package com.example.gaokaoproject.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class VolunteerPlan {
    private Long id;
    private Long userId;
    private String planName;
    private Integer isLocked;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}