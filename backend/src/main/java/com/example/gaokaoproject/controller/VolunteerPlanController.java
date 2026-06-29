package com.example.gaokaoproject.controller;

import com.example.gaokaoproject.entity.*;
import com.example.gaokaoproject.res.Result;
import com.example.gaokaoproject.service.*;
import com.example.gaokaoproject.util.JwtUtil;
import com.example.gaokaoproject.vo.PlanDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/student/plan")
public class VolunteerPlanController {

    @Autowired
    private VolunteerPlanService volunteerPlanService;

    @Autowired
    private VolunteerPlanDetailService volunteerPlanDetailService;

    @Autowired
    private PlanGenerationService planGenerationService;

    @Autowired
    private StudentScoreService studentScoreService;

    @Autowired
    private InstitutionService institutionService;

    @Autowired
    private EnrollmentPlanService enrollmentPlanService;

    @Autowired
    private JwtUtil jwtUtil;

    private Long getUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("未登录或 Token 格式错误");
        }
        String token = authHeader.substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            throw new RuntimeException("Token 无效或已过期，请重新登录");
        }
        return userId;
    }

    // ==================== 方案 CRUD ====================

    @GetMapping("/list")
    public Result<List<VolunteerPlan>> listByUser(@RequestParam Long userId) {
        List<VolunteerPlan> list = volunteerPlanService.selectByUserId(userId);
        return Result.ok(list);
    }

    @GetMapping("/get")
    public Result<VolunteerPlan> getById(@RequestParam Long id) {
        VolunteerPlan plan = volunteerPlanService.selectById(id);
        return Result.ok(plan);
    }

    @GetMapping("/getLocked")
    public Result<VolunteerPlan> getLockedByUser(@RequestParam Long userId) {
        VolunteerPlan plan = volunteerPlanService.selectLockedPlanByUserId(userId);
        return Result.ok(plan);
    }

    @PostMapping("/add")
    public Result<Long> add(@RequestBody VolunteerPlan plan,
                            @RequestHeader("Authorization") String auth) {
        plan.setUserId(getUserId(auth));
        plan.setIsLocked(plan.getIsLocked() != null ? plan.getIsLocked() : 0);
        plan.setStatus(plan.getStatus() != null ? plan.getStatus() : 0);
        volunteerPlanService.insertVolunteerPlan(plan);
        return Result.ok(plan.getId());
    }

    @PutMapping("/update")
    public Result<Integer> update(@RequestBody VolunteerPlan plan) {
        Integer rows = volunteerPlanService.updateVolunteerPlan(plan);
        return Result.ok(rows);
    }

    @DeleteMapping("/delete")
    public Result<Integer> delete(@RequestParam Long id) {
        volunteerPlanDetailService.deleteByPlanId(id);
        Integer rows = volunteerPlanService.deleteVolunteerPlan(id);
        return Result.ok(rows);
    }

    // ==================== AI 生成方案 ====================

    @Transactional
    @PostMapping("/generate")
    public Result<VolunteerPlan> generate(
            @RequestParam String planName,
            @RequestParam(required = false) String sessionId,
            @RequestHeader("Authorization") String auth) {

        Long userId = getUserId(auth);

        // 1. 创建空方案
        VolunteerPlan plan = new VolunteerPlan();
        plan.setUserId(userId);
        plan.setPlanName(planName);
        plan.setIsLocked(0);
        plan.setStatus(0);
        volunteerPlanService.insertVolunteerPlan(plan);

        // 2. AI 生成详情（传入会话ID以关联对话上下文）
        planGenerationService.generatePlanDetails(userId, plan.getId(), sessionId);

        // 3. 返回方案
        return Result.ok(volunteerPlanService.selectById(plan.getId()));
    }

    // ==================== 风险分析（动态生成，不持久化） ====================

    /**
     * 获取方案的 AI 风险分析（每次调用实时生成）
     */
    @GetMapping("/riskAnalysis")
    public Result<String> riskAnalysis(@RequestParam Long planId,
                                       @RequestHeader("Authorization") String auth) {
        Long userId = getUserId(auth);
        VolunteerPlan plan = volunteerPlanService.selectById(planId);
        if (plan == null || !plan.getUserId().equals(userId)) {
            return Result.error("方案不存在或无权操作");
        }

        // 查成绩
        List<StudentScore> scores = studentScoreService.selectByUserId(userId);
        if (scores == null || scores.isEmpty()) {
            return Result.ok(defaultRiskAnalysis());
        }

        // 查详情
        List<VolunteerPlanDetail> details = volunteerPlanDetailService.selectByPlanId(planId);

        try {
            String analysis = planGenerationService.generateRiskAnalysis(
                    userId, scores.get(0), details);
            return Result.ok(analysis != null ? analysis : defaultRiskAnalysis());
        } catch (Exception e) {
            return Result.ok(defaultRiskAnalysis());
        }
    }

    // ==================== 方案详情（含院校/专业信息） ====================

    @GetMapping("/detail/list")
    public Result<List<PlanDetailVO>> listDetails(@RequestParam Long planId) {
        List<VolunteerPlanDetail> details = volunteerPlanDetailService.selectByPlanId(planId);

        // AI 缓存兜底（DB 匹配不上时从这里取名字）
        java.util.List<java.util.Map<String, Object>> aiCache =
                com.example.gaokaoproject.service.impl.PlanGenerationServiceImpl.AI_PLAN_CACHE.get(planId);

        List<PlanDetailVO> voList = new ArrayList<>();
        for (int i = 0; i < details.size(); i++) {
            VolunteerPlanDetail d = details.get(i);
            PlanDetailVO vo = new PlanDetailVO();
            vo.setId(d.getId());
            vo.setPlanId(d.getPlanId());
            vo.setInstitutionId(d.getInstitutionId());
            vo.setEnrollmentPlanId(d.getEnrollmentPlanId());
            vo.setSortOrder(d.getSortOrder());
            vo.setProbability(d.getProbability());
            vo.setRiskLevel(d.getRiskLevel());

            // 优先：数据库匹配（institutionId > 0 说明匹配上了）
            if (d.getInstitutionId() != null && d.getInstitutionId() > 0) {
                Institution inst = institutionService.selectById(d.getInstitutionId());
                if (inst != null) {
                    vo.setInstitutionName(inst.getInstitutionName());
                    vo.setInstitutionLevel(inst.getLevel());
                }
            }
            if (d.getEnrollmentPlanId() != null && d.getEnrollmentPlanId() > 0) {
                EnrollmentPlan ep = enrollmentPlanService.selectById(d.getEnrollmentPlanId());
                if (ep != null) {
                    vo.setMajorName(ep.getMajorName());
                }
            }

            // 兜底：院校或专业任一未匹配到 DB 时，从 AI 缓存补全
            if (aiCache != null && i < aiCache.size()) {
                java.util.Map<String, Object> ai = aiCache.get(i);
                if (vo.getInstitutionName() == null) {
                    vo.setInstitutionName(objToString(ai.get("institutionName")));
                }
                if (vo.getMajorName() == null) {
                    vo.setMajorName(objToString(ai.get("majorName")));
                }
            }

            voList.add(vo);
        }

        return Result.ok(voList);
    }

    // ==================== 锁定/解锁 ====================

    @PostMapping("/lock")
    public Result<Void> lockPlan(@RequestParam Long planId,
                                 @RequestHeader("Authorization") String auth) {
        Long userId = getUserId(auth);
        VolunteerPlan plan = volunteerPlanService.selectById(planId);
        if (plan == null || !plan.getUserId().equals(userId)) {
            return Result.error("方案不存在或无权操作");
        }
        plan.setIsLocked(1);
        plan.setStatus(1);
        volunteerPlanService.updateVolunteerPlan(plan);
        return Result.ok();
    }

    @PostMapping("/unlock")
    public Result<Void> unlockPlan(@RequestParam Long planId,
                                   @RequestHeader("Authorization") String auth) {
        Long userId = getUserId(auth);
        VolunteerPlan plan = volunteerPlanService.selectById(planId);
        if (plan == null || !plan.getUserId().equals(userId)) {
            return Result.error("方案不存在或无权操作");
        }
        plan.setIsLocked(0);
        volunteerPlanService.updateVolunteerPlan(plan);
        return Result.ok();
    }

    // ==================== 导出报告 ====================

    @GetMapping("/export")
    public Result<Map<String, Object>> exportReport(@RequestParam Long planId,
                                                     @RequestHeader("Authorization") String auth) {
        Long userId = getUserId(auth);
        VolunteerPlan plan = volunteerPlanService.selectById(planId);
        if (plan == null || !plan.getUserId().equals(userId)) {
            return Result.error("方案不存在或无权操作");
        }

        List<VolunteerPlanDetail> details = volunteerPlanDetailService.selectByPlanId(planId);

        // 动态生成风险分析
        String riskAnalysis = defaultRiskAnalysis();
        List<StudentScore> scores = studentScoreService.selectByUserId(userId);
        if (scores != null && !scores.isEmpty()) {
            try {
                String ai = planGenerationService.generateRiskAnalysis(userId, scores.get(0), details);
                if (ai != null && !ai.isBlank()) riskAnalysis = ai;
            } catch (Exception ignored) {}
        }

        String html = buildReportHtml(plan, details, riskAnalysis);

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("planId", plan.getId());
        report.put("planName", plan.getPlanName());
        report.put("riskAnalysis", riskAnalysis);
        report.put("html", html);

        return Result.ok(report);
    }

    // ==================== 私有：报告 HTML 构建 ====================

    private String buildReportHtml(VolunteerPlan plan, List<VolunteerPlanDetail> details, String riskAnalysis) {
        StringBuilder rowsHtml = new StringBuilder();

        // AI 缓存兜底
        java.util.List<java.util.Map<String, Object>> aiCache =
                com.example.gaokaoproject.service.impl.PlanGenerationServiceImpl.AI_PLAN_CACHE.get(plan.getId());

        // 按院校分组：同一院校的多个专业合并到一个单元格
        if (details != null && !details.isEmpty()) {
            // 用 LinkedHashMap 保持顺序，key = 院校名
            java.util.LinkedHashMap<String, java.util.List<String[]>> schoolMap = new java.util.LinkedHashMap<>();
            java.util.LinkedHashMap<String, Integer> schoolRisk = new java.util.LinkedHashMap<>();

            for (int i = 0; i < details.size(); i++) {
                VolunteerPlanDetail d = details.get(i);
                String instName = "";
                String majorName = "";

                // DB 匹配
                if (d.getInstitutionId() != null && d.getInstitutionId() > 0) {
                    Institution inst = institutionService.selectById(d.getInstitutionId());
                    if (inst != null) instName = inst.getInstitutionName();
                }
                if (d.getEnrollmentPlanId() != null && d.getEnrollmentPlanId() > 0) {
                    EnrollmentPlan ep = enrollmentPlanService.selectById(d.getEnrollmentPlanId());
                    if (ep != null) majorName = ep.getMajorName();
                }

                // AI 缓存兜底
                if (aiCache != null && i < aiCache.size()) {
                    java.util.Map<String, Object> ai = aiCache.get(i);
                    if (instName.isEmpty()) instName = objToString(ai.get("institutionName"));
                    if (majorName.isEmpty()) majorName = objToString(ai.get("majorName"));
                }

                if (instName.isEmpty()) instName = "未知院校";
                if (majorName.isEmpty()) majorName = "未知专业";

                String prob = d.getProbability() != null
                        ? d.getProbability().setScale(0, java.math.RoundingMode.HALF_UP) + "%"
                        : "—";

                schoolMap.putIfAbsent(instName, new java.util.ArrayList<>());
                schoolMap.get(instName).add(new String[]{majorName, prob});
                schoolRisk.putIfAbsent(instName, d.getRiskLevel() != null ? d.getRiskLevel() : 0);
            }

            int idx = 1;
            for (var entry : schoolMap.entrySet()) {
                String instName = entry.getKey();
                java.util.List<String[]> majors = entry.getValue();
                int risk = schoolRisk.getOrDefault(instName, 0);

                String riskLabel = switch (risk) {
                    case 1 -> "🔴 冲刺";
                    case 2 -> "🟡 稳妥";
                    case 3 -> "🟢 保底";
                    default -> "—";
                };

                // 专业合并为一个单元格
                StringBuilder majorsCell = new StringBuilder();
                for (int j = 0; j < majors.size(); j++) {
                    if (j > 0) majorsCell.append("<br>");
                    majorsCell.append(escapeHtml(majors.get(j)[0]))
                              .append("（").append(majors.get(j)[1]).append("）");
                }

                rowsHtml.append("<tr>")
                        .append("<td>").append(idx++).append("</td>")
                        .append("<td>").append(escapeHtml(instName)).append("</td>")
                        .append("<td>").append(majorsCell.toString()).append("</td>")
                        .append("<td>").append(riskLabel).append("</td>")
                        .append("</tr>");
            }
        }

        String riskHtml = (riskAnalysis != null && !riskAnalysis.isBlank())
                ? "<div class='risk-box'>" + escapeHtml(riskAnalysis).replace("\n", "<br>") + "</div>"
                : "";

        String timeStr = plan.getUpdatedAt() != null
                ? plan.getUpdatedAt().toString()
                : plan.getCreatedAt() != null ? plan.getCreatedAt().toString() : "";

        return """
            <!DOCTYPE html>
            <html lang="zh-CN">
            <head>
                <meta charset="UTF-8">
                <title>%s - 高考志愿方案报告</title>
                <style>
                    body { font-family: 'SimSun', 'Microsoft YaHei', serif; max-width: 850px; margin: 0 auto; padding: 30px; color: #333; }
                    h1 { text-align: center; font-size: 26px; margin-bottom: 5px; }
                    .subtitle { text-align: center; color: #666; font-size: 13px; margin-bottom: 15px; }
                    .risk-box { background: #fff8e1; border-left: 4px solid #ff9800; padding: 12px 16px; margin: 20px 0; font-size: 14px; line-height: 1.8; border-radius: 4px; }
                    table { width: 100%%; border-collapse: collapse; margin-top: 20px; }
                    th, td { border: 1px solid #ddd; padding: 9px 12px; text-align: left; font-size: 13px; vertical-align: top; }
                    th { background-color: #f5f5f5; font-weight: bold; }
                    tr:nth-child(even) { background-color: #fafafa; }
                    .footer { text-align: center; color: #999; font-size: 12px; margin-top: 35px; padding-top: 15px; border-top: 1px solid #eee; }
                    @media print { body { padding: 0; } .risk-box { background: #fff8e1 !important; -webkit-print-color-adjust: exact; } }
                </style>
            </head>
            <body>
                <h1>📋 高考志愿方案报告</h1>
                <div class="subtitle">方案名称：%s | 生成时间：%s</div>
                %s
                <p><strong>说明：</strong>本报告基于历年录取数据和您的成绩信息由 AI 智能生成，仅供参考。</p>
                <table>
                    <thead>
                        <tr><th>序号</th><th>院校名称</th><th>推荐专业（录取概率）</th><th>梯度</th></tr>
                    </thead>
                    <tbody>%s</tbody>
                </table>
                <div class="footer">本报告由高考志愿填报系统自动生成 · 仅供参考 · 不构成最终报考依据</div>
            </body>
            </html>
            """.formatted(
                escapeHtml(plan.getPlanName()),
                escapeHtml(plan.getPlanName()),
                timeStr,
                riskHtml,
                rowsHtml.toString()
            );
    }

    private static String defaultRiskAnalysis() {
        return "⚠️ 风险提示：\n"
                + "1. 冲刺志愿录取概率较低（30%-60%），建议不超过总志愿数的30%\n"
                + "2. 稳妥志愿是您的核心选择区间（60%-85%），请仔细对比院校和专业\n"
                + "3. 保底志愿建议至少填报3所以上，确保有学可上\n"
                + "4. 以上建议基于历年数据，实际录取受多种因素影响，仅供参考";
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static String objToString(Object obj) {
        return obj == null ? null : obj.toString();
    }
}
