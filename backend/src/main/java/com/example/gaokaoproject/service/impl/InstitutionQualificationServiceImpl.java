package com.example.gaokaoproject.service.impl;

import com.example.gaokaoproject.entity.Institution;
import com.example.gaokaoproject.entity.InstitutionQualification;
import com.example.gaokaoproject.exception.ServiceException;
import com.example.gaokaoproject.mapper.InstitutionMapper;
import com.example.gaokaoproject.mapper.InstitutionQualificationMapper;
import com.example.gaokaoproject.service.InstitutionQualificationService;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class InstitutionQualificationServiceImpl implements InstitutionQualificationService {
    @Resource
    private InstitutionQualificationMapper qualMapper;

    @Resource
    private InstitutionMapper institutionMapper;

    @Override
    public List<InstitutionQualification> getWaitReviewPage(int pageNum, int pageSize, String keyword, Long institutionId) {
        PageHelper.startPage(pageNum, pageSize);
        return qualMapper.selectWaitReviewPage(keyword, institutionId);
    }

    @Override
    public InstitutionQualification getDetail(Long id) {
        InstitutionQualification data = qualMapper.selectById(id);
        if(data == null){
            throw new ServiceException(400, "资质记录不存在");
        }
        return data;
    }

    @Override
    public Integer pass(Long id, String comment, Long adminId) {
        InstitutionQualification qual = getDetail(id);
        // 只能对待审核操作
        if(qual.getReviewStatus() != 0){
            throw new ServiceException(4001, "仅待审核单据可执行通过操作");
        }
        int rows = qualMapper.updateReview(id, 1, comment, adminId);
        // 审核通过后，同步更新对应院校 status 为"已通过"
        Institution institution = institutionMapper.selectById(qual.getInstitutionId());
        if (institution != null) {
            institution.setStatus(1);
            institutionMapper.update(institution);
        }
        return rows;
    }

    @Override
    public Integer reject(Long id, String comment, Long adminId) {
        InstitutionQualification qual = getDetail(id);
        if(qual.getReviewStatus() != 0){
            throw new ServiceException(4002, "仅待审核单据可执行驳回操作");
        }
        // 驳回强制要求填写意见
        if(comment == null || comment.trim().isEmpty()){
            throw new ServiceException(4003, "驳回必须填写审核意见");
        }
        return qualMapper.updateReview(id, 2, comment, adminId);
    }

    @Override
    public List<InstitutionQualification> getReviewHistory(String keyword) {
        return qualMapper.selectReviewHistory(keyword);
    }

    @Override
    public List<InstitutionQualification> selectByInstitutionId(Long institutionId) {
        return qualMapper.selectByInstitutionId(institutionId);
    }

    @Override
    public List<InstitutionQualification> selectAll() {
        return qualMapper.selectAll();
    }

    @Override
    public InstitutionQualification selectById(Long id) {
        return qualMapper.selectById(id);
    }

    @Override
    public Integer update(InstitutionQualification institutionQualification) {
        return qualMapper.update(institutionQualification);
    }

    @Override
    public Integer add(InstitutionQualification institutionQualification) {
        return qualMapper.add(institutionQualification);
    }

    @Override
    public Integer delete(Long id) {
        return qualMapper.delete(id);
    }
}