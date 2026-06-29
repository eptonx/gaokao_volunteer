package com.example.gaokaoproject.controller;

import com.example.gaokaoproject.entity.Admin;
import com.example.gaokaoproject.entity.InstitutionQualification;
import com.example.gaokaoproject.res.Result;
import com.example.gaokaoproject.service.InstitutionQualificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/qualification")
public class InstitutionQualificationController {

    @Autowired
    private InstitutionQualificationService qualificationService;

    @GetMapping("/selectAll")
    public List<InstitutionQualification> selectAll() {
        return qualificationService.selectAll();
    }

    @GetMapping("/getOne")
    public InstitutionQualification getOne(@RequestParam Long id) {
        return qualificationService.selectById(id);
    }

    @PostMapping("/add")
    public Integer add(@RequestBody InstitutionQualification qualification) {
        return qualificationService.add(qualification);
    }

    @PutMapping("/update")
    public Integer update(@RequestBody InstitutionQualification qualification) {
        return qualificationService.update(qualification);
    }

    @DeleteMapping("/delete")
    public Integer delete(@RequestParam Long id) {
        return qualificationService.delete(id);
    }

    // 查询某院校的资质审核状态
    @GetMapping("/byInstitution")
    public List<InstitutionQualification> byInstitution(@RequestParam Long institutionId) {
        return qualificationService.selectByInstitutionId(institutionId);
    }

    // 1、待审核分页列表（支持按文件名、院校ID搜索）
    @GetMapping("/waitList")
    public Result<List<InstitutionQualification>> waitList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long institutionId
    ){
        List<InstitutionQualification> list = qualificationService.getWaitReviewPage(pageNum, pageSize, keyword, institutionId);
        return Result.ok(list);
    }

    // 2、查看资质详情
    @GetMapping("/detail")
    public Result<InstitutionQualification> detail(@RequestParam Long id){
        InstitutionQualification data = qualificationService.getDetail(id);
        return Result.ok(data);
    }

    // 3、通过审核
    @PostMapping("/pass")
    public Result<Integer> pass(
            @RequestParam Long id,
            @RequestParam String comment,
            HttpServletRequest request
    ){
        // 获取当前登录管理员
        HttpSession session = request.getSession();
        Admin loginAdmin = (Admin) session.getAttribute("loginAdmin");
        Integer rows = qualificationService.pass(id, comment, loginAdmin.getId());
        return Result.ok(rows);
    }

    // 4、驳回审核
    @PostMapping("/reject")
    public Result<Integer> reject(
            @RequestParam Long id,
            @RequestParam String comment,
            HttpServletRequest request
    ){
        HttpSession session = request.getSession();
        Admin loginAdmin = (Admin) session.getAttribute("loginAdmin");
        Integer rows = qualificationService.reject(id, comment, loginAdmin.getId());
        return Result.ok(rows);
    }

    // 5、审核历史列表（支持搜索）
    @GetMapping("/history")
    public Result<List<InstitutionQualification>> history(
            @RequestParam(required = false) String keyword
    ){
        List<InstitutionQualification> list = qualificationService.getReviewHistory(keyword);
        return Result.ok(list);
    }
}