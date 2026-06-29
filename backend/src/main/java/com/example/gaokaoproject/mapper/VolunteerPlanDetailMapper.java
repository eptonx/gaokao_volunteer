package com.example.gaokaoproject.mapper;

import com.example.gaokaoproject.entity.VolunteerPlanDetail;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface VolunteerPlanDetailMapper {

    // 根据方案ID查询所有志愿明细
    @Select("SELECT * FROM volunteer_plan_detail WHERE plan_id = #{planId} ORDER BY sort_order ASC")
    List<VolunteerPlanDetail> selectByPlanId(@Param("planId") Long planId);

    // 根据ID查询单个明细
    @Select("SELECT * FROM volunteer_plan_detail WHERE id = #{id}")
    VolunteerPlanDetail selectById(@Param("id") Long id);

    // 插入一条志愿明细
    @Insert("INSERT INTO volunteer_plan_detail(plan_id, institution_id, enrollment_plan_id, sort_order, probability, risk_level) " +
            "VALUES(#{planId}, #{institutionId}, #{enrollmentPlanId}, #{sortOrder}, #{probability}, #{riskLevel})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    Integer insertVolunteerPlanDetail(VolunteerPlanDetail detail);

    // 批量插入志愿明细（生成方案时使用）
    @Insert("<script>" +
            "INSERT INTO volunteer_plan_detail(plan_id, institution_id, enrollment_plan_id, sort_order, probability, risk_level) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.planId}, #{item.institutionId}, #{item.enrollmentPlanId}, #{item.sortOrder}, #{item.probability}, #{item.riskLevel})" +
            "</foreach>" +
            "</script>")
    Integer batchInsert(@Param("list") List<VolunteerPlanDetail> details);

    // 更新志愿明细
    @Update("UPDATE volunteer_plan_detail SET institution_id = #{institutionId}, enrollment_plan_id = #{enrollmentPlanId}, " +
            "sort_order = #{sortOrder}, probability = #{probability}, risk_level = #{riskLevel} WHERE id = #{id}")
    Integer updateVolunteerPlanDetail(VolunteerPlanDetail detail);

    // 删除某个方案的所有明细
    @Delete("DELETE FROM volunteer_plan_detail WHERE plan_id = #{planId}")
    Integer deleteByPlanId(@Param("planId") Long planId);
}