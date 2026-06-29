package com.example.gaokaoproject.mapper;

import com.example.gaokaoproject.entity.AdmissionHistory;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AdmissionHistoryMapper {

    @Select("select * from admission_history")
    List<AdmissionHistory> selectAll();

    @Select("select * from admission_history where id = #{id}")
    AdmissionHistory selectById(Long id);

    // ========== 志愿方案生成专用查询 ==========

    /**
     * 根据省份 + 年份 + 科类 查询所有录取记录（用于冲稳保匹配）
     */
    @Select("SELECT ah.*, i.institution_name FROM admission_history ah " +
            "LEFT JOIN institution i ON ah.institution_id = i.id " +
            "WHERE ah.province_code = #{provinceCode} AND ah.year = #{year} " +
            "AND ah.category_code = #{categoryCode} " +
            "ORDER BY ah.min_rank ASC")
    List<AdmissionHistory> selectByProvinceYearCategory(
            @Param("provinceCode") String provinceCode,
            @Param("year") Integer year,
            @Param("categoryCode") String categoryCode);

    /**
     * 查询某院校某年的录取记录
     */
    @Select("select * from admission_history " +
            "where institution_id = #{institutionId} and year = #{year} " +
            "and province_code = #{provinceCode}")
    List<AdmissionHistory> selectByInstitutionYearProvince(
            @Param("institutionId") Long institutionId,
            @Param("year") Integer year,
            @Param("provinceCode") String provinceCode);

    /**
     * 按位次范围查询 —— 冲稳保核心查询
     * @param provinceCode 省份代码
     * @param year 年份
     * @param categoryCode 科类代码
     * @param minRank 最低位次（含）
     * @param maxRank 最高位次（含）
     */
    @Select("SELECT ah.* FROM admission_history ah " +
            "WHERE ah.province_code = #{provinceCode} AND ah.year = #{year} " +
            "AND ah.category_code = #{categoryCode} " +
            "AND ah.min_rank BETWEEN #{minRank} AND #{maxRank} " +
            "ORDER BY ah.min_rank ASC")
    List<AdmissionHistory> selectByRankRange(
            @Param("provinceCode") String provinceCode,
            @Param("year") Integer year,
            @Param("categoryCode") String categoryCode,
            @Param("minRank") Integer minRank,
            @Param("maxRank") Integer maxRank);

    @Insert("insert into admission_history (institution_id, major_name, year, province_code, category_code, " +
            "min_score, max_score, avg_score, min_rank, enrollment_count) values (#{institutionId}, #{majorName}, " +
            "#{year}, #{provinceCode}, #{categoryCode}, #{minScore}, #{maxScore}, #{avgScore}, #{minRank}, #{enrollmentCount})")
    int add(AdmissionHistory history);
    @Insert("insert into admission_history (institution_id, major_id, major_name, year, province_code, category_code, " +
            "min_score, max_score, avg_score, min_rank, enrollment_count, status) values (#{institutionId}, #{majorId}, " +
            "#{majorName}, #{year}, #{provinceCode}, #{categoryCode}, #{minScore}, #{maxScore}, #{avgScore}, " +
            "#{minRank}, #{enrollmentCount}, #{status})")
    int insert(AdmissionHistory history);

    @Update("update admission_history set institution_id = #{institutionId}, major_name = #{majorName}, " +
            "year = #{year}, province_code = #{provinceCode}, category_code = #{categoryCode}, " +
            "min_score = #{minScore}, max_score = #{maxScore}, avg_score = #{avgScore}, " +
            "min_rank = #{minRank}, enrollment_count = #{enrollmentCount} where id = #{id}")
    int update(AdmissionHistory history);

    @Delete("delete from admission_history where id = #{id}")
    int delete(Long id);

    /**
     * 根据院校ID查询录取历史
     * @param institutionId 院校ID
     * @return 录取历史列表
     */
    @Select("SELECT * FROM admission_history WHERE institution_id = #{institutionId} ORDER BY year DESC")
    List<AdmissionHistory> selectByInstitutionId(@Param("institutionId") Long institutionId);

    @Select("<script>" +
            "SELECT * FROM admission_history WHERE 1=1 " +
            "<if test='institutionId != null'> AND institution_id = #{institutionId}</if> " +
            "ORDER BY id DESC LIMIT #{offset}, #{pageSize}" +
            "</script>")
    List<AdmissionHistory> selectPage(@Param("institutionId") Long institutionId,
                                       @Param("offset") int offset,
                                       @Param("pageSize") int pageSize);

    @Select("SELECT COUNT(*) FROM admission_history WHERE institution_id = #{institutionId}")
    int countByInstitution(@Param("institutionId") Long institutionId);

    @Delete("delete from admission_history where id = #{id}")
    int deleteById(Long id);

    @Update("update admission_history set institution_id = #{institutionId}, major_id = #{majorId}, " +
            "major_name = #{majorName}, year = #{year}, province_code = #{provinceCode}, " +
            "category_code = #{categoryCode}, min_score = #{minScore}, max_score = #{maxScore}, " +
            "avg_score = #{avgScore}, min_rank = #{minRank}, enrollment_count = #{enrollmentCount}, " +
            "status = #{status} where id = #{id}")
    int updateById(AdmissionHistory history);

}