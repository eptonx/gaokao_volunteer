package com.example.gaokaoproject.controller;

import com.example.gaokaoproject.entity.EnrollmentPlan;
import com.example.gaokaoproject.service.EnrollmentPlanService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/plan")
@Tag(name = "招生计划端")
public class EnrollmentPlanController {

    @Autowired
    private EnrollmentPlanService planService;

    @GetMapping("/selectAll")
    public List<EnrollmentPlan> selectAll() {
        return planService.selectAll();
    }

    @GetMapping("/getOne")
    public EnrollmentPlan getOne(@RequestParam Long id) {
        return planService.selectById(id);
    }

    @PostMapping("/add")
    public Integer add(@RequestBody EnrollmentPlan plan) {
        return planService.add(plan);
    }

    @PutMapping("/update")
    public Integer update(@RequestBody EnrollmentPlan plan) {
        return planService.update(plan);
    }

    @DeleteMapping("/delete")
    public Integer delete(@RequestParam Long id) {
        return planService.delete(id);
    }

    // ========== 新增接口 ==========

    // 分页列表（按院校ID和年份筛选）
    @GetMapping("/pageList")
    public List<EnrollmentPlan> pageList(@RequestParam(required = false) Long institutionId,
                                          @RequestParam(required = false) Integer year,
                                          @RequestParam(defaultValue = "1") Integer pageNum,
                                          @RequestParam(defaultValue = "10") Integer pageSize) {
        return planService.selectPage(institutionId, year, pageNum, pageSize);
    }

    // Excel批量导入（前端解析Excel后传JSON数组）
    @PostMapping("/batchAdd")
    public Integer batchAdd(@RequestBody List<EnrollmentPlan> plans) {
        return planService.batchAdd(plans);
    }

    // 发布/撤回 (status: 1=发布, 0=撤回)
    @PutMapping("/publish")
    public Integer publish(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        Integer status = Integer.valueOf(body.get("status").toString());
        return planService.changePublishStatus(id, status);
    }
}