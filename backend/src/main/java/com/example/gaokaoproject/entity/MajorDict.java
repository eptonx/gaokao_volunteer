package com.example.gaokaoproject.entity;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MajorDict {
    private Long id;
    private String code;
    private String name;
    private String category;
    private String subCategory;
    private String description;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}