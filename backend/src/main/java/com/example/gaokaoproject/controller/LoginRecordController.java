package com.example.gaokaoproject.controller;

import com.example.gaokaoproject.entity.LoginRecord;
import com.example.gaokaoproject.service.LoginRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login-record")
public class LoginRecordController {

    @Autowired
    private LoginRecordService loginRecordService;

    // 1. 新增登录记录
    @PostMapping
    public String add(@RequestBody LoginRecord loginRecord) {
        int count = loginRecordService.addLoginRecord(loginRecord);
        return count > 0 ? "新增成功" : "新增失败";
    }

    // 2. 根据ID删除
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        int count = loginRecordService.deleteLoginRecordById(id);
        return count > 0 ? "删除成功" : "删除失败";
    }

    // 3. 根据ID修改
    @PutMapping
    public String update(@RequestBody LoginRecord loginRecord) {
        int count = loginRecordService.updateLoginRecordById(loginRecord);
        return count > 0 ? "修改成功" : "修改失败";
    }

    // 4. 根据ID查询单条
    @GetMapping("/{id}")
    public LoginRecord getById(@PathVariable Long id) {
        return loginRecordService.getLoginRecordById(id);
    }

    // 5. 查询所有记录
    @GetMapping("/list")
    public List<LoginRecord> list() {
        return loginRecordService.getAllLoginRecords();
    }

    // 6. 根据用户ID查询登录记录（按时间倒序）
    @GetMapping("/list/{userId}")
    public List<LoginRecord> listByUserId(@PathVariable Long userId) {
        return loginRecordService.getLoginRecordsByUserId(userId);
    }

    // === M6 分页+条件查询 ===
    @GetMapping("/page")
    public Map<String, Object> pageList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        LoginRecord query = new LoginRecord();
        query.setUserId(userId);
        query.setStatus(status);
        int offset = (pageNum - 1) * pageSize;
        return loginRecordService.pageList(query, offset, pageSize, startTime, endTime);
    }
}