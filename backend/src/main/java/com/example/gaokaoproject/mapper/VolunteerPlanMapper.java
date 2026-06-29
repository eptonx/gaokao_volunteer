package com.example.gaokaoproject.mapper;

import com.example.gaokaoproject.entity.VolunteerPlan;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface VolunteerPlanMapper {

    // 根据用户ID查询所有志愿方案
    @Select("SELECT * FROM volunteer_plan WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<VolunteerPlan> selectByUserId(@Param("userId") Long userId);

    // 根据ID查询单个方案
    @Select("SELECT * FROM volunteer_plan WHERE id = #{id}")
    VolunteerPlan selectById(@Param("id") Long id);

    // 查询用户已锁定的方案（每个用户最多一个锁定方案）
    @Select("SELECT * FROM volunteer_plan WHERE user_id = #{userId} AND is_locked = 1")
    VolunteerPlan selectLockedPlanByUserId(@Param("userId") Long userId);

    // 查询用户的历史锁定记录（包括已锁定和已解锁的方案）
    @Select("SELECT * FROM volunteer_plan WHERE user_id = #{userId} ORDER BY is_locked DESC, updated_at DESC")
    List<VolunteerPlan> selectLockRecordsByUserId(@Param("userId") Long userId);

    // 插入新方案（注意字段映射，created_at/updated_at 自动生成）
    @Insert("INSERT INTO volunteer_plan(user_id, plan_name, is_locked, status, created_at, updated_at) " +
            "VALUES(#{userId}, #{planName}, #{isLocked}, #{status}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    Integer insertVolunteerPlan(VolunteerPlan plan);

    // 更新方案信息（注意更新时间）
    @Update("UPDATE volunteer_plan SET plan_name = #{planName}, is_locked = #{isLocked}, " +
            "status = #{status}, updated_at = NOW() WHERE id = #{id}")
    Integer updateVolunteerPlan(VolunteerPlan plan);

    // 删除方案（物理删除，如有需要可改为软删除）
    @Delete("DELETE FROM volunteer_plan WHERE id = #{id}")
    Integer deleteVolunteerPlan(@Param("id") Long id);
}