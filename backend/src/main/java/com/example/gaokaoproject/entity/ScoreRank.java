package com.example.gaokaoproject.entity;

import lombok.Data;

/**
 * 一分一段表实体 — 对应 score_rank 表
 */
@Data
public class ScoreRank {
    private Long id;
    private Integer year;            // 年份
    private String subjectType;      // 科类：物理类/历史类
    private Integer scoreMin;        // 分数下界
    private Integer scoreMax;        // 分数上界
    private String scoreLabel;       // 分数标签：如 "691" 或 "695-750"
    private Integer segmentCount;    // 本段人数
    private Integer cumulativeCount; // 累计人数（位次）
    private String provinceCode;     // 省份代码
    private String remark;           // 备注
}
