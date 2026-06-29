package com.example.gaokaoproject.service.impl;

import com.example.gaokaoproject.entity.*;
import com.example.gaokaoproject.mapper.*;
import com.example.gaokaoproject.service.InstitutionService;
import com.example.gaokaoproject.service.EnrollmentPlanService;
import com.example.gaokaoproject.service.PlanGenerationService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * AI 志愿方案生成服务 —— 完全由大模型生成 3冲4稳3保 方案
 */
@Service
public class PlanGenerationServiceImpl implements PlanGenerationService {

    private static final Logger log = LoggerFactory.getLogger(PlanGenerationServiceImpl.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /** AI 生成方案的院校/专业名称兜底缓存：planId → [{institutionName, majorName, probability, riskLevel}] */
    public static final java.util.concurrent.ConcurrentHashMap<Long, List<Map<String, Object>>> AI_PLAN_CACHE
            = new java.util.concurrent.ConcurrentHashMap<>();

    @Autowired
    private StudentScoreMapper studentScoreMapper;

    @Autowired
    private VolunteerPlanDetailMapper volunteerPlanDetailMapper;

    @Autowired
    private AiConversationMapper aiConversationMapper;

    @Autowired
    private InstitutionService institutionService;

    @Autowired
    private EnrollmentPlanService enrollmentPlanService;

    @Autowired
    private ChatClient chatClient;

    @Override
    public List<VolunteerPlanDetail> generatePlanDetails(Long userId, Long planId, String sessionId) {
        // 1. 查询考生成绩
        List<StudentScore> scores = studentScoreMapper.selectByUserId(userId);
        if (scores == null || scores.isEmpty()) {
            throw new RuntimeException("请先录入成绩信息");
        }
        StudentScore score = scores.get(0);

        log.info("AI 生成志愿方案: userId={}, planId={}, score={}, rank={}, sessionId={}",
                userId, planId, score.getTotalScore(), score.getProvinceRank(), sessionId);

        // 2. 构建 AI Prompt（含对话上下文）
        String prompt = buildPrompt(score, sessionId);

        // 3. 调用 AI 生成结构化回复
        String aiResponse = callAi(prompt);
        log.info("AI 回复长度: {}", aiResponse != null ? aiResponse.length() : 0);

        // 4. 解析 AI 回复为方案详情
        List<VolunteerPlanDetail> details = parseAiResponse(aiResponse, planId);

        // 5. 校验并打日志（AI 已通过 prompt 约束按 3冲4稳3保 输出，不再强制降级）
        java.util.List<java.util.Map<String, Object>> aiCache = AI_PLAN_CACHE.get(planId);
        if (aiCache != null && !aiCache.isEmpty()) {
            java.util.Set<String> chongSchools = new java.util.HashSet<>();
            java.util.Set<String> wenSchools = new java.util.HashSet<>();
            java.util.Set<String> baoSchools = new java.util.HashSet<>();
            for (java.util.Map<String, Object> entry : aiCache) {
                String name = objToString(entry.get("institutionName"));
                Object rl = entry.get("riskLevel");
                int risk = rl instanceof Number ? ((Number) rl).intValue() : 0;
                if (name != null && !name.isBlank()) {
                    if (risk == 1) chongSchools.add(name);
                    else if (risk == 2) wenSchools.add(name);
                    else if (risk == 3) baoSchools.add(name);
                }
            }
            log.info("AI 生成结果: 冲={}所, 稳={}所, 保={}所, 总条目={}",
                    chongSchools.size(), wenSchools.size(), baoSchools.size(), details.size());
        } else {
            log.info("AI 生成结果: 总条目={}", details.size());
        }

        // 仅在 AI 完全无输出时才降级
        if (details.isEmpty()) {
            log.warn("AI 未生成任何条目，使用降级方案");
            details = buildFallbackPlan(score, planId);
        }

        // 6. 排序并写入数据库
        for (int i = 0; i < details.size(); i++) {
            details.get(i).setSortOrder(i + 1);
            details.get(i).setPlanId(planId);
        }

        if (!details.isEmpty()) {
            volunteerPlanDetailMapper.batchInsert(details);
            log.info("志愿详情写入成功: planId={}, count={}", planId, details.size());
        }

        return details;
    }

    @Override
    public String generateRiskAnalysis(Long userId, StudentScore score, List<VolunteerPlanDetail> details) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请对以下高考志愿方案进行风险分析：\n\n");
        prompt.append("【考生情况】\n");
        prompt.append("- 总分：").append(score.getTotalScore()).append("分\n");
        prompt.append("- 全省位次：第").append(score.getProvinceRank()).append("名\n");
        prompt.append("- 省份：").append(score.getProvinceCode()).append("\n");
        prompt.append("- 科类：").append(score.getSubjectType() == 1 ? "物理类" : "历史类").append("\n\n");

        prompt.append("【方案概况】\n");
        long chong = details.stream().filter(d -> d.getRiskLevel() == 1).count();
        long wen = details.stream().filter(d -> d.getRiskLevel() == 2).count();
        long bao = details.stream().filter(d -> d.getRiskLevel() == 3).count();
        prompt.append("- 冲刺志愿：").append(chong).append("个\n");
        prompt.append("- 稳妥志愿：").append(wen).append("个\n");
        prompt.append("- 保底志愿：").append(bao).append("个\n\n");

        prompt.append("请从梯度结构、风险点、改进建议三方面分析，控制在200字以内，直接输出不要问候语。");

        try {
            return chatClient.prompt().user(prompt.toString()).call().content();
        } catch (Exception e) {
            log.error("AI 风险分析调用失败", e);
            return defaultRiskAnalysis();
        }
    }

    // ==================== Prompt 构建 ====================

    private String buildPrompt(StudentScore score, String sessionId) {
        String provinceName = getProvinceName(score.getProvinceCode());
        String subjectName = score.getSubjectType() == 1 ? "物理类" : "历史类";

        StringBuilder sb = new StringBuilder();
        sb.append("""
            你是一位资深高考志愿填报专家。请根据以下考生信息，为其生成一份志愿填报方案。

            【考生信息】
            - 省份：%s
            - 科类：%s
            - 总分：%d分
            - 全省位次：第%d名
            - 选考科目：%s
            """.formatted(
                provinceName,
                subjectName,
                score.getTotalScore(),
                score.getProvinceRank() != null ? score.getProvinceRank() : 0,
                score.getSelectedSubjects() != null ? score.getSelectedSubjects() : "未填写"
            ));

        // 如果传入了会话ID，把最近的对话历史作为上下文，让生成的方案与聊天内容一致
        if (sessionId != null && !sessionId.isBlank()) {
            List<AiConversation> history = aiConversationMapper.selectBySessionId(sessionId);
            if (history != null && !history.isEmpty()) {
                // 只取最近 10 轮对话，避免 prompt 过长
                int start = Math.max(0, history.size() - 20);
                sb.append("""

                    【此前的 AI 对话记录】
                    以下是你与考生此前的完整对话。你的任务是：从对话中提取你推荐过的所有院校和专业，
                    整理成 JSON 格式输出。只使用对话中已出现过的院校和专业，不要编造新的。
                    如果对话中推荐的院校不足 10 所或专业不足，用同层次院校和专业补充，
                    但仍优先、大量使用对话中已推荐过的内容。
                    """);
                for (int i = start; i < history.size(); i++) {
                    AiConversation msg = history.get(i);
                    String roleLabel = msg.getRole() == 1 ? "考生" : "AI助手";
                    sb.append(roleLabel).append("：").append(msg.getContent()).append("\n");
                }
                sb.append("\n");
            }
        }

        sb.append("""

            【要求】
            1. 严格按照「3所冲刺 + 4所稳妥 + 3所保底」的梯度结构，共 10 所院校
            2. 每所院校推荐 1~6 个专业，每个专业单独一条记录
            3. 冲刺院校录取概率约 30%%-60%%，稳妥约 60%%-85%%，保底约 85%%-99%%
            4. 同一院校的不同专业有不同的录取概率是正常的
            5. 院校和专业选择要符合该省份、科类的实际招生情况
            6. 有对话记录时，你是在做"提取整理"而非"重新生成"，直接使用对话中已推荐的院校和专业，不得编造新院校

            【输出格式】请严格按照以下 JSON 数组格式输出（不要输出任何其他文字）：
            [
              {"institutionName":"XX大学","majorName":"计算机科学与技术","probability":42.0,"riskLevel":1},
              {"institutionName":"XX大学","majorName":"软件工程","probability":38.0,"riskLevel":1},
              {"institutionName":"XX大学","majorName":"人工智能","probability":55.0,"riskLevel":1},
              {"institutionName":"XX大学","majorName":"电子信息","probability":50.0,"riskLevel":1},
              {"institutionName":"YY大学","majorName":"临床医学","probability":68.0,"riskLevel":2},
              {"institutionName":"YY大学","majorName":"口腔医学","probability":72.0,"riskLevel":2}
            ]
            注意：不同院校的专业数量应有差异（1~6个不等），不要所有院校都是相同数量的专业。

            riskLevel: 1=冲刺, 2=稳妥, 3=保底
            请确保输出 10 所院校，每所 1~6 个专业，且是合法 JSON 数组。
            """);

        return sb.toString();
    }

    // ==================== AI 调用 ====================

    private String callAi(String prompt) {
        try {
            String content = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            log.info("AI 回复长度: {}", content != null ? content.length() : 0);
            return content;
        } catch (Exception e) {
            log.error("AI 调用失败: {}", e.getMessage(), e);
            throw new RuntimeException("AI 服务暂时不可用，请稍后重试：" + e.getMessage());
        }
    }

    // ==================== 解析 AI 回复 ====================

    private List<VolunteerPlanDetail> parseAiResponse(String aiResponse, Long planId) {
        List<VolunteerPlanDetail> details = new ArrayList<>();
        List<Map<String, Object>> cached = new ArrayList<>();
        if (aiResponse == null || aiResponse.isBlank()) return details;

        try {
            String json = extractJsonArray(aiResponse);
            log.info("提取的 JSON: {}", json.length() > 200 ? json.substring(0, 200) + "..." : json);

            List<Map<String, Object>> items = objectMapper.readValue(
                    json, new TypeReference<List<Map<String, Object>>>() {});

            for (Map<String, Object> item : items) {
                String instName = objToString(item.get("institutionName"));
                String majorName = objToString(item.get("majorName"));

                Object prob = item.get("probability");
                double probVal = prob instanceof Number ? ((Number) prob).doubleValue() : 50.0;

                Object rl = item.get("riskLevel");
                int riskVal = rl instanceof Number ? ((Number) rl).intValue() : 2;

                if (instName != null && !instName.isBlank()) {
                    VolunteerPlanDetail d = new VolunteerPlanDetail();
                    d.setPlanId(planId);
                    d.setProbability(BigDecimal.valueOf(probVal).setScale(1, RoundingMode.HALF_UP));
                    d.setRiskLevel(riskVal);

                    // 1. 尝试匹配 institution 表 → 拿真实 ID
                    Institution inst = institutionService.selectByName(instName);
                    if (inst != null) {
                        d.setInstitutionId(inst.getId());
                        if (majorName != null && !majorName.isBlank()) {
                            EnrollmentPlan ep = enrollmentPlanService.selectByInstitutionIdAndMajorName(inst.getId(), majorName);
                            d.setEnrollmentPlanId(ep != null ? ep.getId() : 0L);
                        } else {
                            d.setEnrollmentPlanId(0L);
                        }
                        log.info("AI 匹配成功: {} → instId={}", instName, inst.getId());
                    } else {
                        d.setInstitutionId(0L);
                        d.setEnrollmentPlanId(0L);
                        log.info("AI 院校名未匹配 DB: {}，将走缓存兜底", instName);
                    }

                    // 2. 总是写入缓存兜底（DB 匹配不上时至少名字不丢）
                    Map<String, Object> cacheEntry = new LinkedHashMap<>();
                    cacheEntry.put("institutionName", instName);
                    cacheEntry.put("majorName", majorName);
                    cacheEntry.put("probability", probVal);
                    cacheEntry.put("riskLevel", riskVal);
                    cached.add(cacheEntry);

                    details.add(d);
                }
            }

            if (!cached.isEmpty()) {
                AI_PLAN_CACHE.put(planId, cached);
            }
        } catch (Exception e) {
            log.error("解析 AI 回复失败: {}", e.getMessage(), e);
        }

        return details;
    }

    /**
     * 从 AI 回复中提取 JSON 数组
     */
    private String extractJsonArray(String text) {
        int start = text.indexOf('[');
        int end = text.lastIndexOf(']');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    // ==================== 降级方案 ====================

    private List<VolunteerPlanDetail> buildFallbackPlan(StudentScore score, Long planId) {
        List<VolunteerPlanDetail> details = new ArrayList<>();
        List<Map<String, Object>> cached = new ArrayList<>();
        int sortOrder = 1;

        int s = score.getTotalScore() != null ? score.getTotalScore() : 500;
        // 格式：{riskLevel, institutionName, majorName, probability}
        String[][] plan;

        if (s >= 650) {
            plan = new String[][]{
                // 冲刺 3 所
                {"1","清华大学","计算机科学与技术","42.0"},{"1","清华大学","电子信息工程","38.0"},{"1","清华大学","自动化","45.0"},
                {"1","北京大学","数学类","48.0"},{"1","北京大学","物理学","43.0"},{"1","北京大学","计算机科学与技术","50.0"},
                {"1","复旦大学","经济学","55.0"},{"1","复旦大学","金融学","52.0"},{"1","复旦大学","数据科学","58.0"},
                // 稳妥 4 所
                {"2","浙江大学","软件工程","68.0"},{"2","浙江大学","计算机科学与技术","65.0"},{"2","浙江大学","人工智能","72.0"},
                {"2","南京大学","人工智能","72.0"},{"2","南京大学","软件工程","75.0"},{"2","南京大学","电子信息","70.0"},
                {"2","中国科学技术大学","物理学","78.0"},{"2","中国科学技术大学","计算机科学","75.0"},
                {"2","武汉大学","电气工程","82.0"},{"2","武汉大学","电子信息","80.0"},{"2","武汉大学","测绘工程","78.0"},
                // 保底 3 所
                {"3","华中科技大学","机械工程","88.0"},{"3","华中科技大学","电气工程","90.0"},{"3","华中科技大学","计算机科学","85.0"},
                {"3","西安交通大学","能源动力","92.0"},{"3","西安交通大学","电气工程","90.0"},{"3","西安交通大学","机械工程","88.0"},
                {"3","东南大学","电子信息","96.0"},{"3","东南大学","计算机科学","94.0"},{"3","东南大学","建筑学","92.0"},
            };
        } else if (s >= 580) {
            plan = new String[][]{
                // 冲刺 3 所
                {"1","华东师范大学","教育学","40.0"},{"1","华东师范大学","心理学","43.0"},{"1","华东师范大学","汉语言文学","46.0"},
                {"1","华南理工大学","自动化","48.0"},{"1","华南理工大学","计算机科学","45.0"},{"1","华南理工大学","软件工程","50.0"},
                {"1","大连理工大学","船舶工程","55.0"},{"1","大连理工大学","机械工程","52.0"},{"1","大连理工大学","化工","50.0"},
                // 稳妥 4 所
                {"2","重庆大学","土木工程","63.0"},{"2","重庆大学","建筑学","65.0"},{"2","重庆大学","电气工程","68.0"},
                {"2","湖南大学","车辆工程","70.0"},{"2","湖南大学","机械工程","72.0"},{"2","湖南大学","电气工程","68.0"},
                {"2","南京航空航天大学","飞行器设计","76.0"},{"2","南京航空航天大学","自动化","73.0"},{"2","南京航空航天大学","计算机科学","78.0"},
                {"2","北京科技大学","材料科学","81.0"},{"2","北京科技大学","冶金工程","78.0"},{"2","北京科技大学","机械工程","83.0"},
                // 保底 3 所
                {"3","武汉理工大学","交通运输","87.0"},{"3","武汉理工大学","车辆工程","89.0"},{"3","武汉理工大学","材料科学","85.0"},
                {"3","西南交通大学","轨道交通","91.0"},{"3","西南交通大学","土木工程","88.0"},{"3","西南交通大学","交通运输","93.0"},
                {"3","河海大学","水利工程","95.0"},{"3","河海大学","土木工程","93.0"},{"3","河海大学","环境工程","90.0"},
            };
        } else if (s >= 500) {
            plan = new String[][]{
                // 冲刺 3 所
                {"1","广州大学","计算机科学与技术","42.0"},{"1","广州大学","软件工程","45.0"},{"1","广州大学","电子信息","40.0"},
                {"1","深圳大学","电子信息工程","48.0"},{"1","深圳大学","计算机科学","45.0"},{"1","深圳大学","通信工程","50.0"},
                {"1","南京工业大学","化学工程","53.0"},{"1","南京工业大学","材料科学","50.0"},{"1","南京工业大学","生物工程","48.0"},
                // 稳妥 4 所
                {"2","燕山大学","机械设计","64.0"},{"2","燕山大学","材料成型","67.0"},{"2","燕山大学","车辆工程","62.0"},
                {"2","昆明理工大学","冶金工程","70.0"},{"2","昆明理工大学","采矿工程","68.0"},{"2","昆明理工大学","环境工程","72.0"},
                {"2","上海理工大学","光电信息","75.0"},{"2","上海理工大学","机械工程","73.0"},{"2","上海理工大学","能源动力","77.0"},
                {"2","浙江工业大学","制药工程","80.0"},{"2","浙江工业大学","化工","78.0"},{"2","浙江工业大学","生物工程","82.0"},
                // 保底 3 所
                {"3","河南大学","汉语言文学","87.0"},{"3","河南大学","历史学","90.0"},{"3","河南大学","教育学","85.0"},
                {"3","安徽大学","法学","91.0"},{"3","安徽大学","汉语言文学","89.0"},{"3","安徽大学","新闻传播","93.0"},
                {"3","广西大学","土木工程","96.0"},{"3","广西大学","电气工程","94.0"},{"3","广西大学","机械工程","92.0"},
            };
        } else {
            plan = new String[][]{
                // 冲刺 3 所
                {"1","河北师范大学","教育学","44.0"},{"1","河北师范大学","汉语言文学","41.0"},{"1","河北师范大学","心理学","47.0"},
                {"1","安徽师范大学","汉语言文学","49.0"},{"1","安徽师范大学","历史学","46.0"},{"1","安徽师范大学","教育学","52.0"},
                {"1","云南财经大学","会计学","54.0"},{"1","云南财经大学","金融学","51.0"},{"1","云南财经大学","经济学","48.0"},
                // 稳妥 4 所
                {"2","长春理工大学","光电工程","65.0"},{"2","长春理工大学","电子信息","68.0"},{"2","长春理工大学","计算机科学","62.0"},
                {"2","湖南科技大学","采矿工程","70.0"},{"2","湖南科技大学","安全工程","73.0"},{"2","湖南科技大学","土木工程","68.0"},
                {"2","陕西科技大学","轻化工程","75.0"},{"2","陕西科技大学","材料科学","72.0"},{"2","陕西科技大学","食品工程","78.0"},
                {"2","桂林电子科技大学","通信工程","80.0"},{"2","桂林电子科技大学","电子信息","78.0"},{"2","桂林电子科技大学","计算机科学","82.0"},
                // 保底 3 所
                {"3","黑龙江大学","法学","88.0"},{"3","黑龙江大学","汉语言文学","90.0"},{"3","黑龙江大学","新闻传播","86.0"},
                {"3","南通大学","临床医学","92.0"},{"3","南通大学","护理学","94.0"},{"3","南通大学","医学检验","90.0"},
                {"3","温州大学","机械工程","96.0"},{"3","温州大学","电气工程","94.0"},{"3","温州大学","材料科学","92.0"},
            };
        }

        for (String[] row : plan) {
            int riskLevel = Integer.parseInt(row[0]);
            String instName = row[1];
            String majorName = row[2];
            BigDecimal prob = new BigDecimal(row[3]);

            VolunteerPlanDetail d = new VolunteerPlanDetail();
            d.setPlanId(planId);
            d.setRiskLevel(riskLevel);
            d.setProbability(prob);
            d.setSortOrder(sortOrder++);

            // 尝试匹配 institution 表拿真实 ID
            Institution inst = institutionService.selectByName(instName);
            if (inst != null) {
                d.setInstitutionId(inst.getId());
                EnrollmentPlan ep = enrollmentPlanService.selectByInstitutionIdAndMajorName(inst.getId(), majorName);
                d.setEnrollmentPlanId(ep != null ? ep.getId() : 0L);
            } else {
                d.setInstitutionId(0L);
                d.setEnrollmentPlanId(0L);
                log.info("降级院校名未匹配 DB: {}", instName);
            }

            // 缓存兜底
            Map<String, Object> ce = new LinkedHashMap<>();
            ce.put("institutionName", instName);
            ce.put("majorName", majorName);
            ce.put("probability", prob.doubleValue());
            ce.put("riskLevel", riskLevel);
            cached.add(ce);

            details.add(d);
        }

        AI_PLAN_CACHE.put(planId, cached);
        return details;
    }

    private static String defaultRiskAnalysis() {
        return "⚠️ 风险提示：\n"
                + "1. 冲刺志愿录取概率较低（30%-60%），建议不超过总志愿数的30%\n"
                + "2. 稳妥志愿是您的核心选择区间，请仔细对比院校和专业\n"
                + "3. 保底志愿建议至少填报3所以上，确保有学可上\n"
                + "4. 以上建议基于历年数据，实际录取受多种因素影响，仅供参考";
    }

    // ==================== 工具方法 ====================

    private static String objToString(Object obj) {
        return obj == null ? null : obj.toString();
    }

    private static String getProvinceName(String code) {
        if (code == null) return "未知";
        String prefix = code.length() >= 2 ? code.substring(0, 2) : code;
        return switch (prefix) {
            case "11" -> "北京"; case "12" -> "天津"; case "13" -> "河北";
            case "14" -> "山西"; case "15" -> "内蒙古"; case "21" -> "辽宁";
            case "22" -> "吉林"; case "23" -> "黑龙江"; case "31" -> "上海";
            case "32" -> "江苏"; case "33" -> "浙江"; case "34" -> "安徽";
            case "35" -> "福建"; case "36" -> "江西"; case "37" -> "山东";
            case "41" -> "河南"; case "42" -> "湖北"; case "43" -> "湖南";
            case "44" -> "广东"; case "45" -> "广西"; case "46" -> "海南";
            case "50" -> "重庆"; case "51" -> "四川"; case "52" -> "贵州";
            case "53" -> "云南"; case "54" -> "西藏"; case "61" -> "陕西";
            case "62" -> "甘肃"; case "63" -> "青海"; case "64" -> "宁夏";
            case "65" -> "新疆";
            default -> code;
        };
    }
}
