package com.example.gaokaoproject.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StudentScore {
    private Long id;
    private Long userId;
    private String provinceCode;
    private Integer examYear;
    private Integer subjectType;
    private String selectedSubjects;
    private Integer totalScore;
    private Integer provinceRank;
    private BigDecimal equivalentScore;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}