package com.example.gaokaoproject.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InstitutionQualification {
    private Long id;
    private Long institutionId;
    private Integer materialType;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private Integer reviewStatus;
    private String reviewComment;
    private LocalDateTime createdAt;
    private Long reviewAdminId;
    private LocalDateTime reviewedAt;

    // 以下为关联查询展示字段（非数据库字段）
    private String institutionName;
}