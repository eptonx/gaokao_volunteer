package com.example.gaokaoproject.entity;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EnrollmentPlan {
    private Long id;
    private Long institutionId;
    private Integer year;
    private String provinceCode;
    private String batchCode;
    private String categoryCode;
    private String majorCode;
    private String majorName;
    private String majorDirection;
    private Integer planCount;
    private BigDecimal tuitionFee;
    private Integer schoolingLength;
    private String degreeType;
    private Integer educationLevel;
    private String limitSubjects;
    private Integer isNewMajor;
    private String remark;
    private Integer status;
    private Long createdBy;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;
}