package com.example.gaokaoproject.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdmissionGuide {
    private Long id;
    private Long institutionId;
    private Integer year;
    private String title;
    private String content;
    private String sourceFileName;
    private String sourceFileUrl;
    private Integer reviewStatus;     // 0待审核 1通过 2驳回
    private String reviewComment;
    private Long reviewerId;
    private Integer publishStatus;
    private LocalDateTime publishAt;
    private Integer viewCount;
    private Integer downloadCount;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    // 非数据库字段，查询时 JOIN 填充
    private String institutionName;
}
