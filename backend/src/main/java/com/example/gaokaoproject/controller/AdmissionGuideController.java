package com.example.gaokaoproject.controller;

import com.example.gaokaoproject.entity.AdmissionGuide;
import com.example.gaokaoproject.service.AdmissionGuideService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admission-guide")
public class AdmissionGuideController {

    @Autowired
    private AdmissionGuideService admissionGuideService;

    @PostMapping
    public String add(@RequestBody AdmissionGuide guide) {
        int count = admissionGuideService.addAdmissionGuide(guide);
        return count > 0 ? "新增成功" : "新增失败";
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        int count = admissionGuideService.deleteAdmissionGuideById(id);
        return count > 0 ? "删除成功" : "删除失败";
    }

    @PutMapping
    public String update(@RequestBody AdmissionGuide guide) {
        int count = admissionGuideService.updateAdmissionGuideById(guide);
        return count > 0 ? "修改成功" : "修改失败";
    }

    @GetMapping("/{id:\\d+}")
    public AdmissionGuide getById(@PathVariable Long id) {
        return admissionGuideService.getAdmissionGuideById(id);
    }

    @GetMapping("/list")
    public List<AdmissionGuide> list() {
        return admissionGuideService.getAllAdmissionGuides();
    }

    // === M6 审核接口 ===

    @GetMapping("/review-list")
    public List<AdmissionGuide> reviewList() {
        return admissionGuideService.getPendingList();
    }

    @GetMapping("/reviewed-list")
    public List<AdmissionGuide> reviewedList() {
        return admissionGuideService.getReviewedList();
    }

    @PutMapping("/{id}/review")
    public String review(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer reviewStatus = (Integer) body.get("reviewStatus");
        String reviewComment = (String) body.get("reviewComment");
        Long reviewerId = body.get("reviewerId") != null
                ? ((Number) body.get("reviewerId")).longValue() : null;
        if (reviewStatus == null) return "reviewStatus 不能为空";
        int count = admissionGuideService.review(id, reviewStatus, reviewComment, reviewerId);
        return count > 0 ? "审核完成" : "审核失败";
    }

    @GetMapping("/{id}/ai-result")
    public Map<String, Object> aiResult(@PathVariable Long id) {
        AdmissionGuide guide = admissionGuideService.getAdmissionGuideById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("guide", guide);
        result.put("aiExtracted", null);
        result.put("message", "AI提取结果待对接");
        return result;
    }

    // 新增分页查询接口
    @GetMapping("/pageList")
    public List<AdmissionGuide> pageList(@RequestParam(required = false) Long institutionId,
                                         @RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        return admissionGuideService.selectPage(institutionId, pageNum, pageSize);
    }

    // 新增发布/撤回接口
    @PutMapping("/publish")
    public Integer publish(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        Integer publishStatus = Integer.valueOf(body.get("publishStatus").toString());
        return admissionGuideService.changePublishStatus(id, publishStatus);
    }
}