package com.example.gaokaoproject.service;

import com.example.gaokaoproject.entity.InstitutionQualification;
import java.util.List;

public interface InstitutionQualificationService {
    List<InstitutionQualification> selectAll();
    InstitutionQualification selectById(Long id);
    Integer update(InstitutionQualification institutionQualification);
    Integer add(InstitutionQualification institutionQualification);
    Integer delete(Long id);
    // 按院校ID查询资质审核状态
    List<InstitutionQualification> selectByInstitutionId(Long institutionId);
    // 待审核分页列表
    List<InstitutionQualification> getWaitReviewPage(int pageNum, int pageSize, String keyword, Long institutionId);
    // 单条详情
    InstitutionQualification getDetail(Long id);
    // 通过审核 status=1
    Integer pass(Long id, String comment, Long adminId);
    // 驳回审核 status=2
    Integer reject(Long id, String comment, Long adminId);
    // 审核历史
    List<InstitutionQualification> getReviewHistory(String keyword);
}