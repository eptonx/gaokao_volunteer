package com.example.gaokaoproject.service.impl;

import com.example.gaokaoproject.entity.LoginRecord;
import com.example.gaokaoproject.mapper.DashboardMapper;
import com.example.gaokaoproject.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private DashboardMapper mapper;

    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> r = new HashMap<>();
        try { r.put("userCount", mapper.countUsers()); } catch (Exception e) { r.put("userCount", 0); }
        try { r.put("institutionCount", mapper.countInstitutions()); } catch (Exception e) { r.put("institutionCount", 0); }
        try { r.put("planCount", mapper.countPlans()); } catch (Exception e) { r.put("planCount", 0); }
        try { r.put("todayActive", mapper.countTodayActive()); } catch (Exception e) { r.put("todayActive", 0); }
        try { r.put("publishedPlanCount", mapper.countPublishedPlans()); } catch (Exception e) { r.put("publishedPlanCount", 0); }
        try { r.put("pendingGuideCount", mapper.countPendingGuides()); } catch (Exception e) { r.put("pendingGuideCount", 0); }
        try { r.put("onlineInstitutionCount", mapper.countOnlineInstitutions()); } catch (Exception e) { r.put("onlineInstitutionCount", 0); }
        try { r.put("todayNewUsers", mapper.countTodayNewUsers()); } catch (Exception e) { r.put("todayNewUsers", 0); }
        return r;
    }

    @Override
    public Map<String, Object> getTrends() {
        Map<String, Object> r = new HashMap<>();
        try { r.put("trends", mapper.loginTrends30Days()); } catch (Exception e) { r.put("trends", Collections.emptyList()); }
        return r;
    }

    @Override
    public Map<String, Object> getTopInstitutions() {
        Map<String, Object> r = new HashMap<>();
        try { r.put("top10", mapper.top10Institutions()); } catch (Exception e) { r.put("top10", Collections.emptyList()); }
        return r;
    }

    @Override
    public Map<String, Object> getProvinceDistribution() {
        Map<String, Object> r = new HashMap<>();
        try { r.put("distribution", mapper.provinceDistribution()); } catch (Exception e) { r.put("distribution", Collections.emptyList()); }
        return r;
    }

    @Override
    public Map<String, Object> getLevelDistribution() {
        Map<String, Object> r = new HashMap<>();
        try { r.put("levels", mapper.institutionLevelDistribution()); } catch (Exception e) { r.put("levels", Collections.emptyList()); }
        return r;
    }

    @Override
    public Map<String, Object> getReviewOverview() {
        Map<String, Object> r = new HashMap<>();
        try { r.put("review", mapper.reviewStatusOverview()); } catch (Exception e) { r.put("review", Collections.emptyList()); }
        return r;
    }

    @Override
    public List<LoginRecord> getRecentLogins() {
        try { return mapper.recentLogins(); } catch (Exception e) { return Collections.emptyList(); }
    }
}
