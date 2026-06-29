package com.example.gaokaoproject.controller;

import com.example.gaokaoproject.entity.Institution;
import com.example.gaokaoproject.service.InstitutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.gaokaoproject.service.InstitutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 院校库管理 — 运营端
 * 管理员维护管控字段：上下线 / 985&211属性 / 审核状态
 */
@RestController
@RequestMapping("/institution")
public class InstitutionController {

    @Autowired
    private InstitutionService institutionService;

    // ==================== 基础 CRUD ====================

    @PostMapping("/add")
    public Integer add(@RequestBody Institution institution) {
        return institutionService.add(institution);
    }

    @PutMapping("/update")
    public Integer update(@RequestBody Institution institution) {
        return institutionService.update(institution);
    }

    @DeleteMapping("/delete/{id}")
    public Integer delete(@PathVariable Long id) {
        return institutionService.delete(id);
    }

    @GetMapping("/list")
    public List<Institution> list() {
        return institutionService.selectAll();
    }

    @GetMapping("/{id}")
    public Institution getById(@PathVariable Long id) {
        return institutionService.selectById(id);
    }

    // ==================== 运营端管控接口 ====================

    /** 上下线 */
    @PutMapping("/onlineStatus")
    public Integer updateOnlineStatus(@RequestParam Long id, @RequestParam Integer isOnline) {
        return institutionService.updateOnlineStatus(id, isOnline);
    }

    /** 改 985/211 属性 */
    @PutMapping("/level")
    public Integer updateLevel(@RequestParam Long id, @RequestParam String level) {
        return institutionService.updateLevel(id, level);
    }

    /** 强制改审核状态 */
    @PutMapping("/status")
    public Integer updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        return institutionService.updateStatus(id, status);
    }

    // 院校自维护：更新名称、代码、logo、简介、官网、联系方式、地址
    @PutMapping("/updateSelf")
    public Institution updateSelf(@RequestBody Institution institution) {
        return institutionService.updateSelfFields(institution);
    }
}
