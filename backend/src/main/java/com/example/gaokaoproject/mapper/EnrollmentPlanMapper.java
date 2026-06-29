package com.example.gaokaoproject.mapper;
import com.example.gaokaoproject.entity.EnrollmentPlan;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface EnrollmentPlanMapper {
    @Select("select id,institution_id,year,province_code,batch_code,category_code,major_code,major_name,major_direction,plan_count,tuition_fee,schooling_length,degree_type,education_level,limit_subjects,is_new_major,remark,status,created_by,published_at,created_at,deleted_at from enrollment_plan")
    List<EnrollmentPlan> selectAll();

    @Select("select id,institution_id,year,province_code,batch_code,category_code,major_code,major_name,major_direction,plan_count,tuition_fee,schooling_length,degree_type,education_level,limit_subjects,is_new_major,remark,status,created_by,published_at,created_at,deleted_at from enrollment_plan where id = #{id}")
    EnrollmentPlan selectById(Long id);

    // ========== 志愿方案生成专用查询 ==========

    /**
     * 根据院校 + 省份 + 年份 + 科类查询招生计划
     */
    @Select("SELECT * FROM enrollment_plan " +
            "WHERE institution_id = #{institutionId} AND province_code = #{provinceCode} " +
            "AND year = #{year} AND category_code = #{categoryCode} " +
            "AND status = 1")
    List<EnrollmentPlan> selectByInstitutionProvinceYearCategory(
            @Param("institutionId") Long institutionId,
            @Param("provinceCode") String provinceCode,
            @Param("year") Integer year,
            @Param("categoryCode") String categoryCode);

    @Update("update enrollment_plan set institution_id=#{institutionId},year=#{year},province_code=#{provinceCode},batch_code=#{batchCode},category_code=#{categoryCode},major_code=#{majorCode},major_name=#{majorName},major_direction=#{majorDirection},plan_count=#{planCount},tuition_fee=#{tuitionFee},schooling_length=#{schoolingLength},degree_type=#{degreeType},education_level=#{educationLevel},limit_subjects=#{limitSubjects},is_new_major=#{isNewMajor},remark=#{remark},status=#{status},created_by=#{createdBy},published_at=#{publishedAt},deleted_at=#{deletedAt} where id=#{id}")
    Integer update(EnrollmentPlan enrollmentPlan);

    @Insert("insert into enrollment_plan(institution_id,year,province_code,batch_code,category_code,major_code,major_name,major_direction,plan_count,tuition_fee,schooling_length,degree_type,education_level,limit_subjects,is_new_major,remark,status,created_by,published_at,created_at,deleted_at) values(#{institutionId},#{year},#{provinceCode},#{batchCode},#{categoryCode},#{majorCode},#{majorName},#{majorDirection},#{planCount},#{tuitionFee},#{schoolingLength},#{degreeType},#{educationLevel},#{limitSubjects},#{isNewMajor},#{remark},#{status},#{createdBy},#{publishedAt},NOW(),#{deletedAt})")
    Integer add(EnrollmentPlan enrollmentPlan);

    @Delete("delete from enrollment_plan where id = #{id}")
    Integer delete(Long id);

    /**
     * 根据院校ID查询招生专业列表
     * @param institutionId 院校ID
     * @return 招生专业列表
     */
    @Select("SELECT * FROM enrollment_plan WHERE institution_id = #{institutionId}")
    List<EnrollmentPlan> selectByInstitutionId(@Param("institutionId") Long institutionId);

    /**
     * 根据院校ID + 专业名称模糊匹配（AI 生成方案时匹配专业 ID）
     */
    @Select("SELECT * FROM enrollment_plan WHERE institution_id = #{institutionId} AND major_name LIKE CONCAT('%', #{majorName}, '%') LIMIT 1")
    EnrollmentPlan selectByInstitutionIdAndMajorName(@Param("institutionId") Long institutionId, @Param("majorName") String majorName);

    // 分页+筛选查询（按院校ID、年份过滤）
    @Select("<script>" +
            "select id,institution_id,year,province_code,batch_code,category_code,major_code,major_name,major_direction,plan_count,tuition_fee,schooling_length,degree_type,education_level,limit_subjects,is_new_major,remark,status,created_by,published_at,created_at,deleted_at from enrollment_plan where 1=1" +
            "<if test='institutionId != null'> and institution_id = #{institutionId}</if>" +
            "<if test='year != null'> and year = #{year}</if>" +
            " order by id desc limit #{offset},#{pageSize}" +
            "</script>")
    List<EnrollmentPlan> selectPage(@Param("institutionId") Long institutionId,
                                    @Param("year") Integer year,
                                    @Param("offset") Integer offset,
                                    @Param("pageSize") Integer pageSize);

    // 批量导入
    @Insert("<script>" +
            "insert into enrollment_plan(institution_id,year,province_code,batch_code,category_code,major_code,major_name,major_direction,plan_count,tuition_fee,schooling_length,degree_type,education_level,limit_subjects,is_new_major,remark,status,created_by,published_at,created_at) values" +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.institutionId},#{item.year},#{item.provinceCode},#{item.batchCode},#{item.categoryCode},#{item.majorCode},#{item.majorName},#{item.majorDirection},#{item.planCount},#{item.tuitionFee},#{item.schoolingLength},#{item.degreeType},#{item.educationLevel},#{item.limitSubjects},#{item.isNewMajor},#{item.remark},0,#{item.createdBy},null,NOW())" +
            "</foreach>" +
            "</script>")
    Integer batchInsert(@Param("list") List<EnrollmentPlan> list);

    // 发布/撤回
    @Update("update enrollment_plan set status = #{status}, published_at = #{publishedAt} where id = #{id}")
    Integer updatePublishStatus(@Param("id") Long id,
                                @Param("status") Integer status,
                                @Param("publishedAt") java.time.LocalDateTime publishedAt);

    // 数据迁移：全量修改 institution_id
    @Update("update enrollment_plan set institution_id = #{toId}")
    Integer migrateAllToInstitution(@Param("toId") Long toId);

    // 工作台：按院校统计招生计划数（institutionId=0 时查全表）
    @Select("<script>" +
            "select count(*) from enrollment_plan where 1=1" +
            "<if test='institutionId != null and institutionId != 0'> and institution_id = #{institutionId}</if>" +
            "</script>")
    Integer countByInstitution(@Param("institutionId") Long institutionId);

    @Select("<script>" +
            "select count(*) from enrollment_plan where status = 1" +
            "<if test='institutionId != null and institutionId != 0'> and institution_id = #{institutionId}</if>" +
            "</script>")
    Integer countPublishedByInstitution(@Param("institutionId") Long institutionId);
}