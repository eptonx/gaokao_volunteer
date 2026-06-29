package com.example.gaokaoproject.service.impl;

import com.example.gaokaoproject.entity.VolunteerPlanDetail;
import com.example.gaokaoproject.mapper.VolunteerPlanDetailMapper;
import com.example.gaokaoproject.service.VolunteerPlanDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VolunteerPlanDetailServiceImpl implements VolunteerPlanDetailService {

    @Autowired
    private VolunteerPlanDetailMapper volunteerPlanDetailMapper;

    @Override
    public List<VolunteerPlanDetail> selectByPlanId(Long planId) {
        return volunteerPlanDetailMapper.selectByPlanId(planId);
    }

    @Override
    public VolunteerPlanDetail selectById(Long id) {
        return volunteerPlanDetailMapper.selectById(id);
    }

    @Override
    public Integer insertVolunteerPlanDetail(VolunteerPlanDetail detail) {
        return volunteerPlanDetailMapper.insertVolunteerPlanDetail(detail);
    }

    @Override
    public Integer batchInsert(List<VolunteerPlanDetail> details) {
        return volunteerPlanDetailMapper.batchInsert(details);
    }

    @Override
    public Integer updateVolunteerPlanDetail(VolunteerPlanDetail detail) {
        return volunteerPlanDetailMapper.updateVolunteerPlanDetail(detail);
    }

    @Override
    public Integer deleteByPlanId(Long planId) {
        return volunteerPlanDetailMapper.deleteByPlanId(planId);
    }
}