package com.example.gaokaoproject.service.impl;

import com.example.gaokaoproject.entity.InstitutionQualification;
import com.example.gaokaoproject.mapper.AdmissionGuideMapper;
import com.example.gaokaoproject.mapper.AdmissionHistoryMapper;
import com.example.gaokaoproject.mapper.EnrollmentPlanMapper;
import com.example.gaokaoproject.mapper.InstitutionQualificationMapper;
import com.example.gaokaoproject.service.WorkbenchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class WorkbenchServiceImpl implements WorkbenchService {

    @Autowired
    private EnrollmentPlanMapper planMapper;
    @Autowired
    private AdmissionGuideMapper guideMapper;
    @Autowired
    private InstitutionQualificationMapper qualificationMapper;
    @Autowired
    private AdmissionHistoryMapper admissionHistoryMapper;

    @Override
    public Map<String, Object> overview(Long institutionId) {
        Map<String, Object> data = new LinkedHashMap<>();

        // 招生计划统计
        int totalPlans = planMapper.countByInstitution(institutionId);
        int publishedPlans = planMapper.countPublishedByInstitution(institutionId);
        Map<String, Object> planStats = new LinkedHashMap<>();
        planStats.put("total", totalPlans);
        planStats.put("published", publishedPlans);
        planStats.put("draft", totalPlans - publishedPlans);
        data.put("planStats", planStats);

        // 招生简章统计
        int totalGuides = guideMapper.countByInstitution(institutionId);
        int publishedGuides = guideMapper.countPublishedByInstitution(institutionId);
        Map<String, Object> guideStats = new LinkedHashMap<>();
        guideStats.put("total", totalGuides);
        guideStats.put("published", publishedGuides);
        guideStats.put("draft", totalGuides - publishedGuides);
        data.put("guideStats", guideStats);

        // 录取分数统计
        int totalScores = admissionHistoryMapper.countByInstitution(institutionId);
        Map<String, Object> scoreStats = new LinkedHashMap<>();
        scoreStats.put("total", totalScores);
        data.put("scoreStats", scoreStats);

        // 资质审核状态
        List<InstitutionQualification> qualifications = qualificationMapper.selectByInstitutionId(institutionId);
        int approved = 0, pending = 0, rejected = 0;
        for (InstitutionQualification q : qualifications) {
            if (q.getReviewStatus() == null || q.getReviewStatus() == 0) pending++;
            else if (q.getReviewStatus() == 1) approved++;
            else if (q.getReviewStatus() == 2) rejected++;
        }
        Map<String, Object> qualStats = new LinkedHashMap<>();
        qualStats.put("total", qualifications.size());
        qualStats.put("approved", approved);
        qualStats.put("pending", pending);
        qualStats.put("rejected", rejected);
        qualStats.put("list", qualifications);
        data.put("qualificationStats", qualStats);

        return data;
    }

    @Override
    public Map<String, Object> migrateData(Long fromId, Long toId) {
        int plans = planMapper.migrateAllToInstitution(toId);
        int guides = guideMapper.migrateAllToInstitution(toId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("migratedPlans", plans);
        result.put("migratedGuides", guides);
        return result;
    }
}
