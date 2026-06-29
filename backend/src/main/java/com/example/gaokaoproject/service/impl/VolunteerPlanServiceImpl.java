package com.example.gaokaoproject.service.impl;

import com.example.gaokaoproject.entity.VolunteerPlan;
import com.example.gaokaoproject.mapper.VolunteerPlanMapper;
import com.example.gaokaoproject.service.VolunteerPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VolunteerPlanServiceImpl implements VolunteerPlanService {

    @Autowired
    private VolunteerPlanMapper volunteerPlanMapper;

    @Override
    public List<VolunteerPlan> selectByUserId(Long userId) {
        return volunteerPlanMapper.selectByUserId(userId);
    }

    @Override
    public VolunteerPlan selectById(Long id) {
        return volunteerPlanMapper.selectById(id);
    }

    @Override
    public VolunteerPlan selectLockedPlanByUserId(Long userId) {
        return volunteerPlanMapper.selectLockedPlanByUserId(userId);
    }

    @Override
    public List<VolunteerPlan> selectLockRecordsByUserId(Long userId) {
        return volunteerPlanMapper.selectLockRecordsByUserId(userId);
    }

    @Override
    public Integer insertVolunteerPlan(VolunteerPlan plan) {
        return volunteerPlanMapper.insertVolunteerPlan(plan);
    }

    @Override
    public Integer updateVolunteerPlan(VolunteerPlan plan) {
        return volunteerPlanMapper.updateVolunteerPlan(plan);
    }

    @Override
    public Integer deleteVolunteerPlan(Long id) {
        return volunteerPlanMapper.deleteVolunteerPlan(id);
    }
}