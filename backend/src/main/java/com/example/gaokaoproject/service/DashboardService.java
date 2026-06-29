package com.example.gaokaoproject.service;

import com.example.gaokaoproject.entity.LoginRecord;
import java.util.List;
import java.util.Map;

public interface DashboardService {
    Map<String, Object> getOverview();
    Map<String, Object> getTrends();
    Map<String, Object> getTopInstitutions();
    Map<String, Object> getProvinceDistribution();
    Map<String, Object> getLevelDistribution();
    Map<String, Object> getReviewOverview();
    List<LoginRecord> getRecentLogins();
}
