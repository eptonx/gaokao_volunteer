package com.example.gaokaoproject.service;

import com.example.gaokaoproject.entity.VolunteerPlanDetail;
import java.util.List;

public interface VolunteerPlanDetailService {
    List<VolunteerPlanDetail> selectByPlanId(Long planId);
    VolunteerPlanDetail selectById(Long id);
    Integer insertVolunteerPlanDetail(VolunteerPlanDetail detail);
    Integer batchInsert(List<VolunteerPlanDetail> details);
    Integer updateVolunteerPlanDetail(VolunteerPlanDetail detail);
    Integer deleteByPlanId(Long planId);
}