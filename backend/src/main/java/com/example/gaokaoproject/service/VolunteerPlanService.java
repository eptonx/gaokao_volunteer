package com.example.gaokaoproject.service;

import com.example.gaokaoproject.entity.VolunteerPlan;
import java.util.List;

public interface VolunteerPlanService {
    /**
     * 根据用户ID查询所有方案
     * @param userId 用户ID
     * @return 方案列表
     */
    List<VolunteerPlan> selectByUserId(Long userId);
    
    /**
     * 根据ID查询单个方案
     * @param id 方案ID
     * @return 方案信息
     */
    VolunteerPlan selectById(Long id);
    
    /**
     * 查询用户当前锁定的方案
     * @param userId 用户ID
     * @return 锁定的方案
     */
    VolunteerPlan selectLockedPlanByUserId(Long userId);
    
    /**
     * 查询用户的历史锁定记录
     * @param userId 用户ID
     * @return 锁定记录列表
     */
    List<VolunteerPlan> selectLockRecordsByUserId(Long userId);
    
    /**
     * 新增方案
     * @param plan 方案信息
     * @return 操作结果
     */
    Integer insertVolunteerPlan(VolunteerPlan plan);
    
    /**
     * 修改方案
     * @param plan 方案信息
     * @return 操作结果
     */
    Integer updateVolunteerPlan(VolunteerPlan plan);
    
    /**
     * 删除方案
     * @param id 方案ID
     * @return 操作结果
     */
    Integer deleteVolunteerPlan(Long id);
}