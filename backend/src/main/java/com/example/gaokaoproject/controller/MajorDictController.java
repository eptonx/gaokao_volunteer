package com.example.gaokaoproject.controller;

import com.example.gaokaoproject.entity.MajorDict;
import com.example.gaokaoproject.service.MajorDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 专业库管理 — 运营端
 * 专业字典 CRUD：增加专业、改名、改大类、上下线
 */
@RestController
@RequestMapping("/majorDict")
public class MajorDictController {

    @Autowired
    private MajorDictService majorDictService;

    @GetMapping("/getById")
    public MajorDict getById(@RequestParam Long id) {
        return majorDictService.getById(id);
    }

    @GetMapping("/list")
    public List<MajorDict> list(@RequestParam(required = false) String majorName) {
        return majorDictService.list(majorName);
    }

    @PostMapping("/save")
    public boolean save(@RequestBody MajorDict dict) {
        return majorDictService.save(dict);
    }

    @PutMapping("/update")
    public boolean update(@RequestBody MajorDict dict) {
        return majorDictService.update(dict);
    }

    @DeleteMapping("/delete")
    public boolean delete(@RequestParam Long id) {
        return majorDictService.delete(id);
    }

    /** 上下线 */
    @PutMapping("/onlineStatus")
    public boolean updateOnlineStatus(@RequestParam Long id, @RequestParam Integer status) {
        return majorDictService.updateOnlineStatus(id, status);
    }
}
