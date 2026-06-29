package com.example.gaokaoproject.controller;

import com.example.gaokaoproject.entity.VolunteerPlanDetail;
import com.example.gaokaoproject.res.Result;
import com.example.gaokaoproject.service.VolunteerPlanDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/student/planDetail")
public class VolunteerPlanDetailController {

    @Autowired
    private VolunteerPlanDetailService volunteerPlanDetailService;

    @GetMapping("/listByPlan")
    public Result<List<VolunteerPlanDetail>> listByPlan(@RequestParam Long planId) {
        List<VolunteerPlanDetail> list = volunteerPlanDetailService.selectByPlanId(planId);
        return Result.ok(list);
    }

    @GetMapping("/get")
    public Result<VolunteerPlanDetail> getById(@RequestParam Long id) {
        VolunteerPlanDetail detail = volunteerPlanDetailService.selectById(id);
        return Result.ok(detail);
    }

    @PostMapping("/add")
    public Result<Integer> add(@RequestBody VolunteerPlanDetail detail) {
        Integer rows = volunteerPlanDetailService.insertVolunteerPlanDetail(detail);
        return Result.ok(rows);
    }

    @PostMapping("/batchAdd")
    public Result<Integer> batchAdd(@RequestBody List<VolunteerPlanDetail> details) {
        Integer rows = volunteerPlanDetailService.batchInsert(details);
        return Result.ok(rows);
    }

    @PutMapping("/update")
    public Result<Integer> update(@RequestBody VolunteerPlanDetail detail) {
        Integer rows = volunteerPlanDetailService.updateVolunteerPlanDetail(detail);
        return Result.ok(rows);
    }

    @DeleteMapping("/deleteByPlan")
    public Result<Integer> deleteByPlan(@RequestParam Long planId) {
        Integer rows = volunteerPlanDetailService.deleteByPlanId(planId);
        return Result.ok(rows);
    }
}
