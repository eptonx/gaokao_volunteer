package com.example.gaokaoproject.service;

import java.math.BigDecimal;

/**
 * 一分一段表服务 —— 等位分换算
 */
public interface ScoreRankService {

    /**
     * 等位分换算：将考生在 fromYear 的分数换算为 toYear 的等效分数
     *
     * @param provinceCode 省份代码（如 420000）
     * @param subjectType  科类（1=物理类, 2=历史类）
     * @param fromYear     考生原始年份
     * @param score        考生原始分数
     * @param toYear       目标年份（如上年，用于参照录取数据）
     * @return 等位分，保留一位小数
     */
    BigDecimal calculateEquivalentScore(String provinceCode, int subjectType,
                                        int fromYear, int score, int toYear);

    /**
     * 根据 (年份, 科类, 位次, 省份) 查位次
     * @return 累计人数（位次），查不到返回 null
     */
    Integer getRank(String provinceCode, int subjectType, int year, int score);

    /**
     * 位次反查分数：根据位次查目标年份对应分数
     * @return 对应分数，查不到返回 null
     */
    BigDecimal rankToScore(String provinceCode, int subjectType, int rank, int targetYear);
}
