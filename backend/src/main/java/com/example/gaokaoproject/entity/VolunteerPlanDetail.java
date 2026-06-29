package com.example.gaokaoproject.entity;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class VolunteerPlanDetail {
    private Long id;
    private Long planId;
    private Long institutionId;
    private Long enrollmentPlanId;
    private Integer sortOrder;
    private BigDecimal probability;
    private Integer riskLevel;
}