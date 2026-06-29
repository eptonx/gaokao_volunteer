package com.example.gaokaoproject.controller;

import com.example.gaokaoproject.service.WorkbenchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/workbench")
@Tag(name = "院校工作台")
public class WorkbenchController {

    @Autowired
    private WorkbenchService workbenchService;

    // 首页数据概览
    @GetMapping("/overview")
    public Map<String, Object> overview(@RequestParam Long institutionId) {
        return workbenchService.overview(institutionId);
    }

    // 数据迁移：把旧 institution_id 的数据批量迁到新 id
    @PutMapping("/migrate")
    public Map<String, Object> migrate(@RequestBody Map<String, Long> body) {
        Long fromId = body.get("fromId");
        Long toId = body.get("toId");
        return workbenchService.migrateData(fromId, toId);
    }
}
