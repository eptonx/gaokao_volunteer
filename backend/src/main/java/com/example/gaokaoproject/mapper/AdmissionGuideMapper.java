package com.example.gaokaoproject.mapper;
import com.example.gaokaoproject.entity.AdmissionGuide;
import org.apache.ibatis.annotations.*;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AdmissionGuideMapper {

    @Select("select id,institution_id,year,title,content,source_file_name,source_file_url,review_status,review_comment,reviewer_id,publish_status,publish_at,view_count,download_count,created_by,created_at,deleted_at from admission_guide")
    List<AdmissionGuide> selectAll();

    @Insert("INSERT INTO admission_guide(institution_id, year, title, content, source_file_name, source_file_url, " +
            "review_status, review_comment, reviewer_id, publish_status, publish_at, view_count, download_count, created_by, created_at) " +
            "VALUES(#{institutionId}, #{year}, #{title}, #{content}, #{sourceFileName}, #{sourceFileUrl}, " +
            "#{reviewStatus}, #{reviewComment}, #{reviewerId}, #{publishStatus}, #{publishAt}, #{viewCount}, #{downloadCount}, #{createdBy}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AdmissionGuide guide);

    @Select("select id,institution_id,year,title,content,source_file_name,source_file_url,review_status,review_comment,reviewer_id,publish_status,publish_at,view_count,download_count,created_by,created_at,deleted_at from admission_guide where id = #{id}")
    AdmissionGuide selectById(Long id);

    @Update("update admission_guide set institution_id=#{institutionId},year=#{year},title=#{title},content=#{content},source_file_name=#{sourceFileName},source_file_url=#{sourceFileUrl},review_status=#{reviewStatus},review_comment=#{reviewComment},reviewer_id=#{reviewerId},publish_status=#{publishStatus},publish_at=#{publishAt},view_count=#{viewCount},download_count=#{downloadCount},created_by=#{createdBy},deleted_at=#{deletedAt} where id=#{id}")
    Integer update(AdmissionGuide admissionGuide);

    @Insert("insert into admission_guide(institution_id,year,title,content,source_file_name,source_file_url,review_status,review_comment,reviewer_id,publish_status,publish_at,view_count,download_count,created_by,created_at,deleted_at) values(#{institutionId},#{year},#{title},#{content},#{sourceFileName},#{sourceFileUrl},#{reviewStatus},#{reviewComment},#{reviewerId},#{publishStatus},NOW(),#{viewCount},#{downloadCount},#{createdBy},NOW(),#{deletedAt})")
    Integer add(AdmissionGuide admissionGuide);

    @Delete("delete from admission_guide where id = #{id}")
    Integer delete(Long id);

    // 新增
    @Select("<script>" +
            "select * from admission_guide where deleted_at is null" +
            "<if test='institutionId != null'> and institution_id = #{institutionId}</if>" +
            " order by id desc limit #{offset},#{pageSize}" +
            "</script>")
    List<AdmissionGuide> selectPage(@Param("institutionId") Long institutionId,
                                    @Param("offset") Integer offset,
                                    @Param("pageSize") Integer pageSize);

    @Update("update admission_guide set publish_status=#{publishStatus},publish_at=#{publishAt} where id=#{id}")
    Integer updatePublishStatus(@Param("id") Long id,
                                @Param("publishStatus") Integer publishStatus,
                                @Param("publishAt") LocalDateTime publishAt);

    @Update("update admission_guide set deleted_at = NOW() where id = #{id}")
    Integer logicDelete(Long id);

    // 数据迁移：全量修改 institution_id
    @Update("update admission_guide set institution_id = #{toId}")
    Integer migrateAllToInstitution(@Param("toId") Long toId);

    // 工作台：按院校统计简章数（institutionId=0 时查全表）
    @Select("<script>" +
            "select count(*) from admission_guide where deleted_at is null" +
            "<if test='institutionId != null and institutionId != 0'> and institution_id = #{institutionId}</if>" +
            "</script>")
    Integer countByInstitution(@Param("institutionId") Long institutionId);

    @Select("<script>" +
            "select count(*) from admission_guide where publish_status = 1 and deleted_at is null" +
            "<if test='institutionId != null and institutionId != 0'> and institution_id = #{institutionId}</if>" +
            "</script>")
    Integer countPublishedByInstitution(@Param("institutionId") Long institutionId);
    @Update("UPDATE admission_guide SET review_status=#{reviewStatus}, review_comment=#{reviewComment}, " +
            "reviewer_id=#{reviewerId} WHERE id=#{id}")
    int updateReviewStatus(@Param("id") Long id, @Param("reviewStatus") Integer reviewStatus,
                           @Param("reviewComment") String reviewComment, @Param("reviewerId") Long reviewerId);

    @Delete("DELETE FROM admission_guide WHERE id=#{id}")
    int deleteById(Long id);

    @Update("UPDATE admission_guide SET institution_id=#{institutionId}, year=#{year}, title=#{title}, " +
            "content=#{content}, source_file_name=#{sourceFileName}, source_file_url=#{sourceFileUrl}, " +
            "publish_status=#{publishStatus}, publish_at=#{publishAt}, view_count=#{viewCount}, " +
            "download_count=#{downloadCount} WHERE id=#{id}")
    int updateById(AdmissionGuide guide);



    @Select("SELECT ag.*, i.institution_name FROM admission_guide ag " +
            "LEFT JOIN institution i ON ag.institution_id = i.id " +
            "WHERE ag.deleted_at IS NULL ORDER BY ag.created_at DESC")
    List<AdmissionGuide> selectAllrun();

    // === M6 审核 ===
    @Select("SELECT ag.*, i.institution_name FROM admission_guide ag " +
            "LEFT JOIN institution i ON ag.institution_id = i.id " +
            "WHERE ag.deleted_at IS NULL AND ag.publish_status = 1 " +
            "AND (ag.review_status = 0 OR ag.review_status IS NULL) " +
            "ORDER BY ag.created_at DESC")
    List<AdmissionGuide> selectPendingList();

    @Select("SELECT ag.*, i.institution_name FROM admission_guide ag " +
            "LEFT JOIN institution i ON ag.institution_id = i.id " +
            "WHERE ag.deleted_at IS NULL AND ag.publish_status = 1 " +
            "AND ag.review_status IN (1, 2) " +
            "ORDER BY ag.created_at DESC LIMIT 20")
    List<AdmissionGuide> selectReviewedList();

    /** 考生端：查询已发布的招生简章 */
    @Select("SELECT * FROM admission_guide WHERE institution_id = #{institutionId} AND publish_status = 1 AND deleted_at IS NULL ORDER BY year DESC")
    List<AdmissionGuide> selectPublishedByInstitutionId(@Param("institutionId") Long institutionId);

}