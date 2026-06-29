package com.example.gaokaoproject.service.impl;

import com.example.gaokaoproject.entity.AdmissionGuide;
import com.example.gaokaoproject.mapper.AdmissionGuideMapper;
import com.example.gaokaoproject.service.AdmissionGuideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdmissionGuideServiceImpl implements AdmissionGuideService {

    @Autowired
    private AdmissionGuideMapper admissionGuideMapper;

    @Override
    public List<AdmissionGuide> selectAll() {
        return admissionGuideMapper.selectAll();
    }

    @Override
    public AdmissionGuide selectById(Long id) {
        return admissionGuideMapper.selectById(id);
    }

    @Override
    public Integer update(AdmissionGuide admissionGuide) {
        return admissionGuideMapper.update(admissionGuide);
    }

    @Override
    public Integer add(AdmissionGuide admissionGuide) {
        return admissionGuideMapper.add(admissionGuide);
    }

    @Override
    public Integer delete(Long id) {
        return admissionGuideMapper.delete(id);
    }

    // 新增分页查询实现
    @Override
    public List<AdmissionGuide> selectPage(Long institutionId, Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return admissionGuideMapper.selectPage(institutionId, offset, pageSize);
    }

    // 新增发布/撤回状态修改实现
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer changePublishStatus(Long id, Integer publishStatus) {
        LocalDateTime publishAt = publishStatus == 1 ? LocalDateTime.now() : null;
        return admissionGuideMapper.updatePublishStatus(id, publishStatus, publishAt);
    }

    // 新增
    @Override
    public int addAdmissionGuide(AdmissionGuide guide) {
        return admissionGuideMapper.insert(guide);
    }

    // 删除
    @Override
    public int deleteAdmissionGuideById(Long id) {
        return admissionGuideMapper.deleteById(id);
    }

    // 更新
    @Override
    public int updateAdmissionGuideById(AdmissionGuide guide) {
        return admissionGuideMapper.updateById(guide);
    }

    // 查单条（只读）
    @Override
    @Transactional(readOnly = true)
    public AdmissionGuide getAdmissionGuideById(Long id) {
        return admissionGuideMapper.selectById(id);
    }

    // 查所有（只读）
    @Override
    @Transactional(readOnly = true)
    public List<AdmissionGuide> getAllAdmissionGuides() {
        return admissionGuideMapper.selectAll();
    }

    // === M6 审核实现 ===
    @Override
    @Transactional(readOnly = true)
    public List<AdmissionGuide> getPendingList() {
        return admissionGuideMapper.selectPendingList().stream()
                .filter(g -> g.getReviewStatus() == null || g.getReviewStatus() == 0)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdmissionGuide> getReviewedList() {
        return admissionGuideMapper.selectReviewedList();
    }

    @Override
    public int review(Long id, Integer reviewStatus, String reviewComment, Long reviewerId) {
        return admissionGuideMapper.updateReviewStatus(id, reviewStatus, reviewComment, reviewerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdmissionGuide> getPublishedByInstitutionId(Long institutionId) {
        return admissionGuideMapper.selectPublishedByInstitutionId(institutionId);
    }
}