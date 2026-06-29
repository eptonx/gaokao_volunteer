package com.example.gaokaoproject.controller;

import com.example.gaokaoproject.entity.LoginRecord;
import com.example.gaokaoproject.service.DashboardService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/overview")
    public Map<String, Object> overview() { return dashboardService.getOverview(); }

    @GetMapping("/trends")
    public Map<String, Object> trends() { return dashboardService.getTrends(); }

    @GetMapping("/top-institutions")
    public Map<String, Object> topInstitutions() { return dashboardService.getTopInstitutions(); }

    @GetMapping("/province-distribution")
    public Map<String, Object> provinceDistribution() { return dashboardService.getProvinceDistribution(); }

    @GetMapping("/level-distribution")
    public Map<String, Object> levelDistribution() { return dashboardService.getLevelDistribution(); }

    @GetMapping("/review-overview")
    public Map<String, Object> reviewOverview() { return dashboardService.getReviewOverview(); }

    @GetMapping("/recent-logins")
    public List<LoginRecord> recentLogins() { return dashboardService.getRecentLogins(); }
}
