package com.example.gaokaoproject.controller;

import com.example.gaokaoproject.entity.StudentScore;
import com.example.gaokaoproject.res.Result;
import com.example.gaokaoproject.service.ScoreRankService;
import com.example.gaokaoproject.service.StudentScoreService;
import com.example.gaokaoproject.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@RestController
@RequestMapping("/student/score")
public class StudentScoreController {

    @Autowired
    private StudentScoreService studentScoreService;

    @Autowired(required = false)
    private ScoreRankService scoreRankService;

    @Autowired
    private JwtUtil jwtUtil;

    private Long getUserId(String authHeader) {
        String token = authHeader.substring(7);
        return jwtUtil.getUserIdFromToken(token);
    }

    @GetMapping("/list")
    public Result<List<StudentScore>> listByUser(@RequestParam Long userId) {
        List<StudentScore> list = studentScoreService.selectByUserId(userId);
        return Result.ok(list);
    }

    @GetMapping("/get")
    public Result<StudentScore> getById(@RequestParam Long id) {
        StudentScore score = studentScoreService.selectById(id);
        return Result.ok(score);
    }

    @PostMapping("/add")
    public Result<Long> add(@RequestBody StudentScore score,
                            @RequestHeader("Authorization") String auth) {
        score.setUserId(getUserId(auth));
        // 自动计算等位分
        score.setEquivalentScore(autoCalcEquivalent(score));
        studentScoreService.insertStudentScore(score);
        return Result.ok(score.getId());
    }

    @PutMapping("/update")
    public Result<Integer> update(@RequestBody StudentScore score,
                                   @RequestHeader("Authorization") String auth) {
        if (score.getId() == null) {
            return Result.error("成绩ID不能为空");
        }
        StudentScore existing = studentScoreService.selectById(score.getId());
        if (existing == null || !existing.getUserId().equals(getUserId(auth))) {
            return Result.error("无权修改");
        }
        // 自动重算等位分
        score.setEquivalentScore(autoCalcEquivalent(score));
        Integer rows = studentScoreService.updateStudentScore(score);
        return Result.ok(rows);
    }

    @DeleteMapping("/delete")
    public Result<Integer> delete(@RequestParam Long id,
                                   @RequestHeader("Authorization") String auth) {
        StudentScore existing = studentScoreService.selectById(id);
        if (existing == null || !existing.getUserId().equals(getUserId(auth))) {
            return Result.error("无权删除");
        }
        Integer rows = studentScoreService.deleteStudentScore(id);
        return Result.ok(rows);
    }

    /**
     * 位次反查分数 —— 根据已保存的位次，换算为目标年份的等效分数
     *
     * 不依赖分数，纯靠位次反查，必须有一分一段表数据
     */
    @GetMapping("/rank-to-score")
    public Result<BigDecimal> rankToScore(
            @RequestParam String provinceCode,
            @RequestParam(defaultValue = "1") Integer subjectType,
            @RequestParam Integer rank,
            @RequestParam Integer targetYear) {

        if (rank == null) {
            return Result.error("位次不能为空");
        }
        if (scoreRankService == null) {
            return Result.error("一分一段表服务未就绪，请重启后端");
        }

        BigDecimal result = scoreRankService.rankToScore(provinceCode, subjectType, rank, targetYear);
        if (result != null) {
            return Result.ok(result);
        }
        return Result.error("一分一段表暂无该省份/科类/年份的数据，请先导入一分一段表");
    }

    /**
     * 等位分换算 —— 优先用一分一段表真实数据，查不到则降级为理论模型
     *
     * 核心思路：
     *   ① 用考生 (年份, 科类, 分数) 查一分一段表 → 得位次
     *   ② 用位次去目标年份一分一段表反查 → 得等位分
     *   ③ 若一分一段表无数据，降级为正态分布理论模型
     */
    @GetMapping("/equivalent")
    public Result<BigDecimal> calcEquivalentScore(
            @RequestParam String provinceCode,
            @RequestParam Integer examYear,
            @RequestParam Integer totalScore,
            @RequestParam Integer provinceRank,
            @RequestParam(required = false, defaultValue = "1") Integer subjectType,
            @RequestParam(required = false) Integer targetYear) {

        if (totalScore == null || provinceRank == null) {
            return Result.error("总分和位次不能为空");
        }

        // 目标年份默认 = 考生年份 -1（用于参照上年录取数据）
        int toYear = targetYear != null ? targetYear : examYear - 1;

        // ① 先试真实一分一段表数据
        if (scoreRankService != null) {
            try {
                BigDecimal realEquivalent = scoreRankService.calculateEquivalentScore(
                        provinceCode, subjectType, examYear, totalScore, toYear);
                if (realEquivalent != null) {
                    return Result.ok(realEquivalent);
                }
            } catch (Exception e) {
                // 真实数据查询失败，降级走理论模型
            }
        }

        // ② 降级：正态分布理论模型
        BigDecimal theoryEquivalent = calcTheoryEquivalent(provinceCode, examYear, totalScore, provinceRank);
        return Result.ok(theoryEquivalent);
    }

    /**
     * 自动计算等位分（成绩保存时调用）
     *
     * 优先级：
     *   ① 用总分查源年份一分一段表得位次 → 用位次查2025年一分一段表得等位分（需两年数据都有）
     *   ② 若源年份数据缺失，直接用考生填的位次查2025年一分一段表（只需2025年数据）
     *   ③ 若2025年数据也没有，降级为正态分布理论模型
     */
    private BigDecimal autoCalcEquivalent(StudentScore score) {
        if (score.getTotalScore() == null || score.getProvinceRank() == null) return null;
        if (score.getProvinceCode() == null || score.getExamYear() == null) return null;
        if (scoreRankService != null) {
            int subjectType = score.getSubjectType() != null ? score.getSubjectType() : 1;
            int toYear = 2025;  // 统一以2025年为标准
            try {
                // ① 优先：完整路径（源年份分数→位次 → 2025年位次→分数）
                BigDecimal eq = scoreRankService.calculateEquivalentScore(
                        score.getProvinceCode(), subjectType, score.getExamYear(), score.getTotalScore(), toYear);
                if (eq != null) return eq;
            } catch (Exception ignored) {}
            try {
                // ② 次选：直接用考生填的位次查2025年一分一段表
                BigDecimal eq = scoreRankService.rankToScore(
                        score.getProvinceCode(), subjectType, score.getProvinceRank(), toYear);
                if (eq != null) return eq;
            } catch (Exception ignored) {}
        }
        // ③ 兜底：理论模型
        return calcTheoryEquivalent(
                score.getProvinceCode() != null ? score.getProvinceCode() : "420000",
                score.getExamYear() != null ? score.getExamYear() : 2025,
                score.getTotalScore(),
                score.getProvinceRank());
    }

    /**
     * 正态分布理论模型（兜底）
     *
     * 核心思路：用位次算出考生在当年所处的百分位，再映射到标准参考分布，
     * 实现跨年份的等位分换算，不再依赖某一年的具体难度。
     */
    private static BigDecimal calcTheoryEquivalent(String provinceCode, int examYear, int totalScore, int provinceRank) {
        long estimatedTotal = estimateTotalCandidates(provinceCode, examYear);
        if (estimatedTotal == 0) {
            return BigDecimal.valueOf(totalScore).setScale(1, RoundingMode.HALF_UP);
        }

        // ① 用位次计算考生在当年的百分位
        //    rank=1（状元）→ 百分位接近 1.0（超过 99.99% 的人）→ Z 值为正（高分）
        //    rank=总人数（垫底）→ 百分位接近 0.0 → Z 值为负（低分）
        double percentile = 1.0 - (double) provinceRank / estimatedTotal;
        percentile = Math.max(0.0001, Math.min(0.9999, percentile));

        // ② 将百分位映射为标准正态分布的 Z 值
        double zScore = inverseNormalCdf(percentile);

        // ③ 用标准参考分布（均值500，标准差100）统一换算等位分
        //    所有年份的分数映射到同一尺度，实现真正的跨年可比
        double referenceMean = 500.0;
        double referenceStd = 100.0;
        double equivalentScore = referenceMean + zScore * referenceStd;

        // 钳制到合理分数范围
        equivalentScore = Math.max(100, Math.min(750, equivalentScore));

        return BigDecimal.valueOf(equivalentScore).setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * 逆正态累积分布函数（Inverse Normal CDF）
     * 使用 Peter J. Acklam 的近似算法，精度 ~1e-9
     */
    private static double inverseNormalCdf(double p) {
        // 常量
        final double A1 = -39.6968302866538;
        final double A2 = 220.9460984245205;
        final double A3 = -275.9285104469687;
        final double A4 = 138.3577518672690;
        final double A5 = -30.66479806614716;
        final double A6 = 2.506628277459239;

        final double B1 = -54.47609879822406;
        final double B2 = 161.5858368580409;
        final double B3 = -155.6989798598866;
        final double B4 = 66.80131188771972;
        final double B5 = -13.28068155288572;

        final double C1 = -7.784894002430293E-3;
        final double C2 = -0.3223964580411365;
        final double C3 = -2.400758277161838;
        final double C4 = -2.549732539343734;
        final double C5 = 4.374664141464968;
        final double C6 = 2.938163982698783;

        final double D1 = 7.784695709041462E-3;
        final double D2 = 0.3224671290700398;
        final double D3 = 2.445134137142996;
        final double D4 = 3.754408661907416;

        // 边界情况
        final double P_LOW = 0.02425;
        final double P_HIGH = 1.0 - P_LOW;

        double q, r;

        if (p < P_LOW) {
            // 下尾区域
            q = Math.sqrt(-2.0 * Math.log(p));
            return (((((C1 * q + C2) * q + C3) * q + C4) * q + C5) * q + C6)
                    / ((((D1 * q + D2) * q + D3) * q + D4) * q + 1.0);
        } else if (p <= P_HIGH) {
            // 中心区域
            q = p - 0.5;
            r = q * q;
            return (((((A1 * r + A2) * r + A3) * r + A4) * r + A5) * r + A6) * q
                    / (((((B1 * r + B2) * r + B3) * r + B4) * r + B5) * r + 1.0);
        } else {
            // 上尾区域
            q = Math.sqrt(-2.0 * Math.log(1.0 - p));
            return -(((((C1 * q + C2) * q + C3) * q + C4) * q + C5) * q + C6)
                    / ((((D1 * q + D2) * q + D3) * q + D4) * q + 1.0);
        }
    }

    /**
     * 估算某省份某年考生总数（单位：万人）
     * provinceCode 支持 6 位（110000）或短码（11）两种格式
     */
    private static long estimateTotalCandidates(String provinceCode, int year) {
        // 取 provinceCode 的前两位作为省份标识
        String prefix = provinceCode.length() >= 2 ? provinceCode.substring(0, 2) : provinceCode;
        return switch (prefix) {
            case "11" -> 55_0000L;  // 北京
            case "12" -> 68_0000L;  // 天津
            case "13" -> 54_0000L;  // 河北
            case "14" -> 34_0000L;  // 山西
            case "15" -> 20_0000L;  // 内蒙古
            case "21" -> 19_0000L;  // 辽宁
            case "22" -> 15_0000L;  // 吉林
            case "23" -> 19_0000L;  // 黑龙江
            case "31" -> 52_0000L;  // 上海
            case "32" -> 44_0000L;  // 江苏
            case "33" -> 38_0000L;  // 浙江
            case "34" -> 59_0000L;  // 安徽
            case "35" -> 24_0000L;  // 福建
            case "36" -> 49_0000L;  // 江西
            case "37" -> 72_0000L;  // 山东
            case "41" -> 98_0000L;  // 河南
            case "42" -> 49_0000L;  // 湖北
            case "43" -> 61_0000L;  // 湖南
            case "44" -> 76_0000L;  // 广东
            case "45" -> 54_0000L;  // 广西
            case "46" -> 71_0000L;  // 海南
            case "50" -> 32_0000L;  // 重庆
            case "51" -> 67_0000L;  // 四川
            case "52" -> 38_0000L;  // 贵州
            case "53" -> 36_0000L;  // 云南
            case "54" -> 36_0000L;  // 西藏
            case "61" -> 32_0000L;  // 陕西
            case "62" -> 24_0000L;  // 甘肃
            case "63" -> 53_0000L;  // 青海
            case "64" -> 75_0000L;  // 宁夏
            case "65" -> 23_0000L;  // 新疆
            default -> 0L;
        };
    }
}
