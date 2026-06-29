package com.example.gaokaoproject.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 志愿详情展示 VO —— 包含院校/专业名称等前端展示字段
 */
@Data
public class PlanDetailVO {
    private Long id;
    private Long planId;
    private Long institutionId;
    private String institutionName;   // 来自 institution 表
    private String institutionLevel;  // 985/211/双一流 等
    private Long enrollmentPlanId;
    private String majorName;         // 来自 enrollment_plan 表
    private Integer sortOrder;
    private BigDecimal probability;   // 录取概率(%)
    private Integer riskLevel;        // 1-冲 2-稳 3-保

    /** 风险等级中文 */
    public String getRiskLabel() {
        return switch (riskLevel != null ? riskLevel : 0) {
            case 1 -> "冲刺";
            case 2 -> "稳妥";
            case 3 -> "保底";
            default -> "未知";
        };
    }
}
