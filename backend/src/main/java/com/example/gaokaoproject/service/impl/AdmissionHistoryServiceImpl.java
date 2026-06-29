package com.example.gaokaoproject.service.impl;

import com.example.gaokaoproject.entity.AdmissionHistory;
import com.example.gaokaoproject.mapper.AdmissionHistoryMapper;
import com.example.gaokaoproject.service.AdmissionHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdmissionHistoryServiceImpl implements AdmissionHistoryService {

    @Autowired
    private AdmissionHistoryMapper mapper;

    @Override
    public List<AdmissionHistory> selectAll() {
        return mapper.selectAll();
    }

    @Override
    public AdmissionHistory selectById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public List<AdmissionHistory> selectByInstitutionId(Long institutionId) {
        return mapper.selectByInstitutionId(institutionId);
    }

    @Override
    public boolean add(AdmissionHistory history) {
        return mapper.insert(history) > 0;
    }

    @Override
    public boolean update(AdmissionHistory history) {
        return mapper.updateById(history) > 0;
    }

    @Override
    public boolean delete(Long id) {
        return mapper.deleteById(id) > 0;
    }

    @Override
    public boolean publish(Long id, Integer status) {
        AdmissionHistory history = new AdmissionHistory();
        history.setId(id);
        history.setStatus(status);
        return mapper.updateById(history) > 0;
    }

    @Override
    public List<AdmissionHistory> selectPage(Long institutionId, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return mapper.selectPage(institutionId, offset, pageSize);
    }
}
