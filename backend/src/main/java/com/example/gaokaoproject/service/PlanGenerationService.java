package com.example.gaokaoproject.service;

import com.example.gaokaoproject.entity.StudentScore;
import com.example.gaokaoproject.entity.VolunteerPlanDetail;

import java.util.List;

/**
 * AI 志愿方案生成服务 — 根据考生成绩自动生成冲稳保梯度方案
 */
public interface PlanGenerationService {

    /**
     * 基于 AI 分析生成志愿方案详情列表
     * @param userId 考生ID
     * @param planId 方案ID（已创建的空方案）
     * @param sessionId 可选，AI 对话会话ID，传入后 AI 将参考对话历史生成方案
     * @return 生成的志愿详情列表（冲稳保排序）
     */
    List<VolunteerPlanDetail> generatePlanDetails(Long userId, Long planId, String sessionId);

    /**
     * 对已有方案生成 AI 风险分析文本（不持久化）
     */
    String generateRiskAnalysis(Long userId, StudentScore score, List<VolunteerPlanDetail> details);
}
