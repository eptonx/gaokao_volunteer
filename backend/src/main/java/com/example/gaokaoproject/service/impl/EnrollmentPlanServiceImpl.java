package com.example.gaokaoproject.service.impl;

import com.example.gaokaoproject.entity.EnrollmentPlan;
import com.example.gaokaoproject.mapper.EnrollmentPlanMapper;
import com.example.gaokaoproject.service.EnrollmentPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EnrollmentPlanServiceImpl implements EnrollmentPlanService {

    @Autowired
    private EnrollmentPlanMapper enrollmentPlanMapper;

    @Override
    public List<EnrollmentPlan> selectAll() {
        return enrollmentPlanMapper.selectAll();
    }

    @Override
    public EnrollmentPlan selectById(Long id) {
        return enrollmentPlanMapper.selectById(id);
    }

    @Override
    public List<EnrollmentPlan> selectByInstitutionId(Long institutionId) {
        return enrollmentPlanMapper.selectByInstitutionId(institutionId);
    }

    @Override
    public Integer update(EnrollmentPlan enrollmentPlan) {
        return enrollmentPlanMapper.update(enrollmentPlan);
    }

    @Override
    public Integer add(EnrollmentPlan enrollmentPlan) {
        return enrollmentPlanMapper.add(enrollmentPlan);
    }

    @Override
    public Integer delete(Long id) {
        return enrollmentPlanMapper.delete(id);
    }

    @Override
    public EnrollmentPlan selectByInstitutionIdAndMajorName(Long institutionId, String majorName) {
        return enrollmentPlanMapper.selectByInstitutionIdAndMajorName(institutionId, majorName);
    }

    // ========== 新增业务方法 ==========

    @Override
    public List<EnrollmentPlan> selectPage(Long institutionId, Integer year, Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return enrollmentPlanMapper.selectPage(institutionId, year, offset, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer batchAdd(List<EnrollmentPlan> plans) {
        if (plans == null || plans.isEmpty()) {
            return 0;
        }
        return enrollmentPlanMapper.batchInsert(plans);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer changePublishStatus(Long id, Integer status) {
        LocalDateTime publishedAt = status == 1 ? LocalDateTime.now() : null;
        return enrollmentPlanMapper.updatePublishStatus(id, status, publishedAt);
    }
}