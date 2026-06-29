package com.example.gaokaoproject.controller;

import com.example.gaokaoproject.entity.DataImportLog;
import com.example.gaokaoproject.service.DataImportLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dataImportLog")
public class DataImportLogController {

    @Autowired
    private DataImportLogService dataImportLogService;

    @GetMapping("/selectAll")
    public List<DataImportLog> selectAll() {
        return dataImportLogService.selectAll();
    }

    @GetMapping("/selectById")
    public DataImportLog selectById(@RequestParam Long id) {
        return dataImportLogService.selectById(id);
    }

    @PostMapping("/add")
    public boolean add(@RequestBody DataImportLog log) {
        return dataImportLogService.add(log);
    }

    @PutMapping("/update")
    public boolean update(@RequestBody DataImportLog log) {
        return dataImportLogService.update(log);
    }

    @DeleteMapping("/delete")
    public boolean delete(@RequestParam Long id) {
        return dataImportLogService.delete(id);
    }
}