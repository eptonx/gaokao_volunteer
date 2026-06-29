package com.example.gaokaoproject.entity;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Attachment {
    private Long id;
    private Integer bizType;
    private Long bizId;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private String fileExt;
    private Long uploadBy;
    private LocalDateTime createdAt;
}