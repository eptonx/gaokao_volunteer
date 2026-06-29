package com.example.gaokaoproject.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AiConversation {
    private Long id;
    private Long userId;
    private String sessionId;
    private Integer role;
    private String content;
    private LocalDateTime createdAt;
}