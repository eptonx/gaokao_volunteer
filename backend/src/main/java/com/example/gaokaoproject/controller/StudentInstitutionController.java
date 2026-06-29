package com.example.gaokaoproject.controller;

import com.example.gaokaoproject.entity.Institution;
import com.example.gaokaoproject.entity.EnrollmentPlan;
import com.example.gaokaoproject.entity.AdmissionHistory;
import com.example.gaokaoproject.entity.AdmissionGuide;
import com.example.gaokaoproject.entity.Favorite;
import com.example.gaokaoproject.res.Result;
import com.example.gaokaoproject.service.InstitutionService;
import com.example.gaokaoproject.service.EnrollmentPlanService;
import com.example.gaokaoproject.service.AdmissionHistoryService;
import com.example.gaokaoproject.service.AdmissionGuideService;
import com.example.gaokaoproject.service.FavoriteService;
import com.example.gaokaoproject.service.StudentInstitutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 考生端院校控制器
 */
@RestController
@RequestMapping("/student/institution")
@Tag(name = "考生院校查询", description = "考生端院校查询相关接口")
public class StudentInstitutionController {

    @Autowired
    private InstitutionService institutionService;

    @Autowired
    private StudentInstitutionService studentInstitutionService;

    @Autowired
    private EnrollmentPlanService enrollmentPlanService;

    @Autowired
    private AdmissionHistoryService admissionHistoryService;

    @Autowired
    private AdmissionGuideService admissionGuideService;

    @Autowired
    private FavoriteService favoriteService;

    /**
     * 多条件筛选查询院校列表（分页）
     * @param params 筛选条件（省份、层次、性质、科类等）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 包含rows和total的Result
     */
    @Operation(summary = "多条件筛选查询院校列表")
    @GetMapping("/list")
    public Result<Map<String, Object>> listInstitutions(
            @RequestParam Map<String, Object> params,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        List<Institution> rows = studentInstitutionService.selectByConditions(params, pageNum, pageSize);
        Long total = studentInstitutionService.countByConditions(params);
        Map<String, Object> data = Map.of("rows", rows, "total", total);
        return Result.ok(data);
    }

    /**
     * 关键词搜索院校（分页）
     */
    @Operation(summary = "关键词搜索院校")
    @GetMapping("/search")
    public Result<Map<String, Object>> searchInstitutions(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        List<Institution> rows = studentInstitutionService.searchByKeyword(keyword, pageNum, pageSize);
        Long total = studentInstitutionService.countByKeyword(keyword);
        Map<String, Object> data = Map.of("rows", rows, "total", total);
        return Result.ok(data);
    }

    /**
     * 查询院校筛选项字典
     * @return 筛选项字典
     */
    @Operation(summary = "查询院校筛选项字典")
    @GetMapping("/filterOptions")
    public Result<Map<String, List<String>>> getFilterOptions() {
        Map<String, List<String>> options = studentInstitutionService.getFilterOptions();
        return Result.ok(options);
    }

    /**
     * 查询院校详情
     * @param id 院校ID
     * @return 院校详情
     */
    @Operation(summary = "查询院校详情")
    @GetMapping("/{id}")
    public Result<Institution> getInstitutionDetail(@PathVariable Long id) {
        Institution institution = institutionService.selectById(id);
        return Result.ok(institution);
    }

    /**
     * 查询院校招生专业列表
     * @param id 院校ID
     * @return 招生专业列表
     */
    @Operation(summary = "查询院校招生专业列表")
    @GetMapping("/{id}/majors")
    public Result<List<EnrollmentPlan>> getInstitutionMajors(@PathVariable Long id) {
        List<EnrollmentPlan> majors = enrollmentPlanService.selectByInstitutionId(id);
        return Result.ok(majors);
    }

    /**
     * 查询院校历年录取分（ECharts图表数据）
     * @param id 院校ID
     * @return 历年录取分数据
     */
    @Operation(summary = "查询院校历年录取分")
    @GetMapping("/{id}/admissionScores")
    public Result<List<AdmissionHistory>> getInstitutionAdmissionScores(@PathVariable Long id) {
        List<AdmissionHistory> scores = admissionHistoryService.selectByInstitutionId(id);
        return Result.ok(scores);
    }

    /**
     * 查询院校已发布招生简章
     * @param id 院校ID
     * @return 招生简章列表
     */
    @Operation(summary = "查询院校招生简章")
    @GetMapping("/{id}/admissionGuides")
    public Result<List<AdmissionGuide>> getAdmissionGuides(@PathVariable Long id) {
        List<AdmissionGuide> guides = admissionGuideService.getPublishedByInstitutionId(id);
        return Result.ok(guides);
    }

    /**
     * 添加收藏（院校或专业）
     * @param userId 用户ID
     * @param targetId 目标ID（院校ID或专业ID）
     * @param type 收藏类型（1:院校, 2:专业）
     * @return 操作结果
     */
    @Operation(summary = "添加收藏")
    @PostMapping("/favorite")
    public Result<Integer> addFavorite(
            @RequestParam Long userId,
            @RequestParam Long targetId,
            @RequestParam Integer type) {
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setTargetId(targetId);
        favorite.setType(type); // 1:院校, 2:专业
        Integer rows = favoriteService.add(favorite);
        return Result.ok(rows);
    }

    /**
     * 取消收藏（院校或专业）
     * @param userId 用户ID
     * @param targetId 目标ID（院校ID或专业ID）
     * @param type 收藏类型（1:院校, 2:专业）
     * @return 操作结果
     */
    @Operation(summary = "取消收藏")
    @DeleteMapping("/favorite")
    public Result<Integer> cancelFavorite(
            @RequestParam Long userId,
            @RequestParam Long targetId,
            @RequestParam Integer type) {
        Integer rows = favoriteService.deleteByUserAndTarget(userId, targetId, type);
        return Result.ok(rows);
    }

    /**
     * 查询我的收藏列表
     * @param userId 用户ID
     * @param type 收藏类型（1:院校, 2:专业）
     * @return 收藏列表
     */
    @Operation(summary = "查询我的收藏列表")
    @GetMapping("/favorite/list")
    public Result<List<Favorite>> getFavorites(
            @RequestParam Long userId,
            @RequestParam(required = false) Integer type) {
        List<Favorite> list;
        if (type != null) {
            list = favoriteService.selectByUserId(userId, type);
        } else {
            list = favoriteService.selectByUserId(userId);
        }
        return Result.ok(list);
    }

    /**
     * 判断是否已收藏
     * @param userId 用户ID
     * @param targetId 目标ID（院校ID或专业ID）
     * @param type 收藏类型（1:院校, 2:专业）
     * @return 是否已收藏
     */
    @Operation(summary = "判断是否已收藏")
    @GetMapping("/check")
    public Result<Boolean> checkFavorite(
            @RequestParam Long userId,
            @RequestParam Long targetId,
            @RequestParam Integer type) {
        boolean exists = favoriteService.countByUserAndTarget(userId, targetId, type) > 0;
        return Result.ok(exists);
    }
}
