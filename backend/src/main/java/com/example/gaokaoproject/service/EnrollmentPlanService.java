package com.example.gaokaoproject.service;

import com.example.gaokaoproject.entity.EnrollmentPlan;
import java.util.List;

public interface EnrollmentPlanService {
    List<EnrollmentPlan> selectAll();
    EnrollmentPlan selectById(Long id);
    List<EnrollmentPlan> selectByInstitutionId(Long institutionId);
    EnrollmentPlan selectByInstitutionIdAndMajorName(Long institutionId, String majorName);
    Integer update(EnrollmentPlan enrollmentPlan);
    Integer add(EnrollmentPlan enrollmentPlan);
    Integer delete(Long id);

    // 分页查询（按院校ID、年份筛选）
    List<EnrollmentPlan> selectPage(Long institutionId, Integer year, Integer pageNum, Integer pageSize);
    // 批量导入
    Integer batchAdd(List<EnrollmentPlan> plans);
    // 发布/撤回
    Integer changePublishStatus(Long id, Integer status);
}
