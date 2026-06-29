package com.example.gaokaoproject.service.impl;

import com.example.gaokaoproject.entity.ScoreRank;
import com.example.gaokaoproject.mapper.ScoreRankMapper;
import com.example.gaokaoproject.service.ScoreRankService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 一分一段表服务实现 — 基于真实数据做等位分换算
 *
 * 核心算法：
 *   ① 用考生原始年份+分数 查一分一段表 → 得到位次
 *   ② 用这个位次 去目标年份一分一段表反查 → 得到对应分数（等位分）
 */
@Service
public class ScoreRankServiceImpl implements ScoreRankService {

    private static final Logger log = LoggerFactory.getLogger(ScoreRankServiceImpl.class);

    @Autowired
    private ScoreRankMapper scoreRankMapper;

    @Override
    public BigDecimal calculateEquivalentScore(String provinceCode, int subjectType,
                                               int fromYear, int score, int toYear) {
        String subTypeStr = toSubjectTypeStr(subjectType);

        // ① 查原始年份的位次
        ScoreRank fromRank = scoreRankMapper.findByScore(fromYear, subTypeStr, score, provinceCode);
        if (fromRank == null) {
            log.warn("一分一段表未查到: year={}, subjectType={}, score={}, province={}",
                    fromYear, subTypeStr, score, provinceCode);
            return null;
        }
        int rank = fromRank.getCumulativeCount();
        log.info("等位分换算: {}年 {} {}分 → 位次={}", fromYear, subTypeStr, score, rank);

        // ② 用位次去目标年份反查分数
        ScoreRank toRank = scoreRankMapper.findByRank(toYear, subTypeStr, rank, provinceCode);
        if (toRank == null) {
            // 位次太高（超过目标年份最高分对应的位次），取目标年份最高分
            Integer maxRank = scoreRankMapper.getMaxRank(toYear, subTypeStr, provinceCode);
            if (maxRank != null && rank <= maxRank) {
                // 这种情况理论上不会发生，兜底再查一次
                log.warn("反查失败但位次在范围内: toYear={}, rank={}, maxRank={}", toYear, rank, maxRank);
            }
            log.warn("等位分反查失败: toYear={}, rank={}, province={}", toYear, rank, provinceCode);
            return null;
        }

        // ③ 取分数下界作为等位分（保守估计）
        BigDecimal equivalent = BigDecimal.valueOf(toRank.getScoreMin())
                .setScale(1, RoundingMode.HALF_UP);

        log.info("等位分结果: {}年 位次={} → 等位分={}", toYear, rank, equivalent);
        return equivalent;
    }

    @Override
    public Integer getRank(String provinceCode, int subjectType, int year, int score) {
        String subTypeStr = toSubjectTypeStr(subjectType);
        ScoreRank sr = scoreRankMapper.findByScore(year, subTypeStr, score, provinceCode);
        return sr != null ? sr.getCumulativeCount() : null;
    }

    @Override
    public BigDecimal rankToScore(String provinceCode, int subjectType, int rank, int targetYear) {
        String subTypeStr = toSubjectTypeStr(subjectType);
        ScoreRank sr = scoreRankMapper.findByRank(targetYear, subTypeStr, rank, provinceCode);
        if (sr == null) {
            log.warn("位次反查失败: year={}, subjectType={}, rank={}, province={}",
                    targetYear, subTypeStr, rank, provinceCode);
            return null;
        }
        log.info("位次反查: {}年 {} 位次{} → 分数{}", targetYear, subTypeStr, rank, sr.getScoreMin());
        return BigDecimal.valueOf(sr.getScoreMin());
    }

    /**
     * 科类 Integer → String
     */
    private static String toSubjectTypeStr(int subjectType) {
        return subjectType == 1 ? "物理类" : "历史类";
    }
}
