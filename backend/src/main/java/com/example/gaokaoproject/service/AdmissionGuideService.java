package com.example.gaokaoproject.service;

import com.example.gaokaoproject.entity.AdmissionGuide;
import java.util.List;

public interface AdmissionGuideService {
    List<AdmissionGuide> selectAll();
    AdmissionGuide selectById(Long id);
    Integer update(AdmissionGuide admissionGuide);
    Integer add(AdmissionGuide admissionGuide);
    Integer delete(Long id);

    // 新增
    List<AdmissionGuide> selectPage(Long institutionId, Integer pageNum, Integer pageSize);
    Integer changePublishStatus(Long id, Integer publishStatus);
    // 新增招生指南
    int addAdmissionGuide(AdmissionGuide guide);

    // 根据ID删除
    int deleteAdmissionGuideById(Long id);

    // 根据ID更新
    int updateAdmissionGuideById(AdmissionGuide guide);

    // 根据ID查询单条
    AdmissionGuide getAdmissionGuideById(Long id);

    // 查询所有记录
    List<AdmissionGuide> getAllAdmissionGuides();

    // === M6 审核 ===
    List<AdmissionGuide> getPendingList();
    List<AdmissionGuide> getReviewedList();
    int review(Long id, Integer reviewStatus, String reviewComment, Long reviewerId);

    /** 考生端：根据院校ID查询已发布招生简章 */
    List<AdmissionGuide> getPublishedByInstitutionId(Long institutionId);
}