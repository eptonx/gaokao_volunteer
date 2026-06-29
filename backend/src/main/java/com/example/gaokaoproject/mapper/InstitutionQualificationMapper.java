package com.example.gaokaoproject.mapper;
import com.example.gaokaoproject.entity.InstitutionQualification;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface InstitutionQualificationMapper {
    @Select("select id,institution_id,material_type,file_name,file_url,file_size,review_status,review_comment,created_at from institution_qualification")
    List<InstitutionQualification> selectAll();

    @Select("select id,institution_id,material_type,file_name,file_url,file_size,review_status,review_comment,created_at from institution_qualification where id = #{id}")
    InstitutionQualification selectById(Long id);

    @Update("update institution_qualification set institution_id=#{institutionId},material_type=#{materialType},file_name=#{fileName},file_url=#{fileUrl},file_size=#{fileSize},review_status=#{reviewStatus},review_comment=#{reviewComment} where id=#{id}")
    Integer update(InstitutionQualification institutionQualification);

    @Insert("insert into institution_qualification(institution_id,material_type,file_name,file_url,file_size,review_status,review_comment,created_at) values(#{institutionId},#{materialType},#{fileName},#{fileUrl},#{fileSize},#{reviewStatus},#{reviewComment},NOW())")
    Integer add(InstitutionQualification institutionQualification);

    @Delete("delete from institution_qualification where id = #{id}")
    Integer delete(Long id);

    // 按院校ID查询所有资质材料（用于查看审核状态）
    @Select("select id,institution_id,material_type,file_name,file_url,file_size,review_status,review_comment,created_at from institution_qualification where institution_id = #{institutionId}")
    List<InstitutionQualification> selectByInstitutionId(Long institutionId);

    // 4、查询审核历史（已通过/驳回：status !=0，JOIN机构名，支持搜索）
    @Select("<script>" +
            "SELECT iq.*, i.institution_name FROM institution_qualification iq " +
            "LEFT JOIN institution i ON iq.institution_id = i.id " +
            "WHERE iq.review_status != 0 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND iq.file_name LIKE CONCAT('%',#{keyword},'%') " +
            "</if>" +
            "ORDER BY iq.reviewed_at DESC" +
            "</script>")
    List<InstitutionQualification> selectReviewHistory(@Param("keyword") String keyword);


    // 1、查询待审核列表 review_status=0（JOIN机构名）
    @Select("SELECT iq.*, i.institution_name FROM institution_qualification iq " +
            "LEFT JOIN institution i ON iq.institution_id = i.id " +
            "WHERE iq.review_status = 0 ORDER BY iq.created_at DESC")
    List<InstitutionQualification> selectWaitReview();

    // 分页待审核（配合PageHelper，支持搜索）
    @Select("<script>" +
            "SELECT iq.*, i.institution_name FROM institution_qualification iq " +
            "LEFT JOIN institution i ON iq.institution_id = i.id " +
            "WHERE iq.review_status = 0 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND iq.file_name LIKE CONCAT('%',#{keyword},'%') " +
            "</if>" +
            "<if test='institutionId != null'>" +
            "AND iq.institution_id = #{institutionId} " +
            "</if>" +
            "ORDER BY iq.created_at DESC" +
            "</script>")
    List<InstitutionQualification> selectWaitReviewPage(@Param("keyword") String keyword, @Param("institutionId") Long institutionId);

    // 2、根据id查资质详情（JOIN机构名）
    @Select("SELECT iq.*, i.institution_name FROM institution_qualification iq " +
            "LEFT JOIN institution i ON iq.institution_id = i.id " +
            "WHERE iq.id = #{id}")
    InstitutionQualification selectByIdrun(Long id);

    // 3、更新审核状态、意见、操作人、审核时间
    @Update("UPDATE institution_qualification SET review_status=#{status},review_comment=#{comment},review_admin_id=#{adminId},reviewed_at=NOW() WHERE id=#{id}")
    int updateReview(
            @Param("id") Long id,
            @Param("status") Integer status,
            @Param("comment") String comment,
            @Param("adminId") Long adminId
    );
}