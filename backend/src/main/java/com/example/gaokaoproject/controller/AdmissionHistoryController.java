package com.example.gaokaoproject.controller;

import com.alibaba.excel.EasyExcel;
import com.example.gaokaoproject.entity.AdmissionHistory;
import com.example.gaokaoproject.entity.AdmissionExcelDTO;
import com.example.gaokaoproject.listener.AdmissionExcelListener;
import com.example.gaokaoproject.service.AdmissionHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

/**
 * 历年录取数据管理 — 运营端
 */
@RestController
@RequestMapping("/admissionHistory")
public class AdmissionHistoryController {

    @Autowired
    private AdmissionHistoryService admissionHistoryService;

    @Autowired
    private AdmissionExcelListener admissionExcelListener;

    // ==================== 基础 CRUD ====================

    @GetMapping("/selectAll")
    public List<AdmissionHistory> selectAll() {
        return admissionHistoryService.selectAll();
    }

    @GetMapping("/pageList")
    public List<AdmissionHistory> pageList(@RequestParam(required = false) Long institutionId,
                                           @RequestParam(defaultValue = "1") int pageNum,
                                           @RequestParam(defaultValue = "10") int pageSize) {
        return admissionHistoryService.selectPage(institutionId, pageNum, pageSize);
    }

    @GetMapping("/selectById")
    public AdmissionHistory selectById(@RequestParam Long id) {
        return admissionHistoryService.selectById(id);
    }

    @PostMapping("/add")
    public boolean add(@RequestBody AdmissionHistory history) {
        return admissionHistoryService.add(history);
    }

    @PutMapping("/update")
    public boolean update(@RequestBody AdmissionHistory history) {
        return admissionHistoryService.update(history);
    }

    @DeleteMapping("/delete")
    public boolean delete(@RequestParam Long id) {
        return admissionHistoryService.delete(id);
    }

    // ==================== Excel 导入 ====================

    /** 下载 Excel 导入模板 */
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("历年录取数据导入模板", "UTF-8");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

        EasyExcel.write(response.getOutputStream(), AdmissionExcelDTO.class)
                .sheet("录取数据")
                .doWrite(new java.util.ArrayList<>());  // 空列表=只导出表头
    }

    /** 批量导入 Excel */
    @PostMapping("/import")
    public String importExcel(@RequestParam("file") MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), AdmissionExcelDTO.class, admissionExcelListener)
                .sheet()
                .doRead();
        return "导入成功";
    }

    // ==================== 发布管理 ====================

    /** 发布 / 取消发布 */
    @PutMapping("/publish")
    public boolean publish(@RequestParam Long id, @RequestParam Integer status) {
        return admissionHistoryService.publish(id, status);
    }
}
