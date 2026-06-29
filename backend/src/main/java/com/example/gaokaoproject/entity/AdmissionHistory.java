package com.example.gaokaoproject.entity;

import lombok.Data;

@Data
public class AdmissionHistory {
    private Long id;
    private Long institutionId;
    private Long majorId;
    private String majorName;
    private Integer year;
    private String provinceCode;
    private String categoryCode;
    private Integer minScore;
    private Integer maxScore;
    private Integer avgScore;
    private Integer minRank;
    private Integer enrollmentCount;
    private Integer status;
}
