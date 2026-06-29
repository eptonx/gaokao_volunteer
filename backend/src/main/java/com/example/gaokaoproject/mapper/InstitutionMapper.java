package com.example.gaokaoproject.mapper;

import com.example.gaokaoproject.entity.Institution;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface InstitutionMapper {
    @Insert("insert into institution(institution_code, institution_name, institution_type, province_code, city_code, address, level, nature, contact_phone, contact_email, official_website, logo_url, introduction, is_online, status, created_at, updated_at) values(#{institutionCode}, #{institutionName}, #{institutionType}, #{provinceCode}, #{cityCode}, #{address}, #{level}, #{nature}, #{contactPhone}, #{contactEmail}, #{officialWebsite}, #{logoUrl}, #{introduction}, #{isOnline}, #{status}, #{createdAt}, #{updatedAt})")
    Integer add(Institution institution);

    @Update("update institution set institution_code = #{institutionCode}, institution_name = #{institutionName}, institution_type = #{institutionType}, province_code = #{provinceCode}, city_code = #{cityCode}, address = #{address}, level = #{level}, nature = #{nature}, contact_phone = #{contactPhone}, contact_email = #{contactEmail}, official_website = #{officialWebsite}, logo_url = #{logoUrl}, introduction = #{introduction}, is_online = #{isOnline}, status = #{status}, updated_at = #{updatedAt} where id = #{id}")
    Integer update(Institution institution);

    @Delete("delete from institution where id = #{id}")
    Integer delete(Long id);

    @Select("select id, institution_code, institution_name, institution_type, province_code, city_code, address, level, nature, contact_phone, contact_email, official_website, logo_url, introduction, is_online, status, created_at, updated_at, deleted_at from institution")
    List<Institution> selectAll();

    @Select("select id, institution_code, institution_name, institution_type, province_code, city_code, address, level, nature, contact_phone, contact_email, official_website, logo_url, introduction, is_online, status, created_at, updated_at, deleted_at from institution where id = #{id}")
    Institution selectById(Long id);

    /**
     * 多条件筛选查询院校列表
     * @param params 筛选条件(provinceCode/level/nature)
     * @param offset 分页偏移量
     * @param pageSize 每页大小
     * @return 院校列表
     */
    @Select("<script>" +
            "SELECT * FROM institution " +
            "<where>" +
            "<if test=\"p.provinceCode != null and p.provinceCode != ''\">AND province_code = #{p.provinceCode}</if>" +
            "<if test=\"p.level != null and p.level != ''\">AND level = #{p.level}</if>" +
            "<if test=\"p.nature != null and p.nature != ''\">AND nature = #{p.nature}</if>" +
            "</where>" +
            "LIMIT #{offset}, #{pageSize}" +
            "</script>")
    List<Institution> selectByConditions(@Param("p") Map<String, Object> params,
                                      @Param("offset") int offset,
                                      @Param("pageSize") int pageSize);

    /**
     * 关键词搜索院校
     * @param keyword 关键词
     * @param offset 分页偏移量
     * @param pageSize 每页大小
     * @return 院校列表
     */
    @Select("SELECT * FROM institution WHERE institution_name LIKE CONCAT('%', #{keyword}, '%') LIMIT #{offset}, #{pageSize}")
    List<Institution> searchByKeyword(@Param("keyword") String keyword,
                                    @Param("offset") int offset,
                                    @Param("pageSize") int pageSize);

    @Select("<script>" +
            "SELECT COUNT(*) FROM institution " +
            "<where>" +
            "<if test=\"provinceCode != null and provinceCode != ''\">AND province_code = #{provinceCode}</if>" +
            "<if test=\"level != null and level != ''\">AND level = #{level}</if>" +
            "<if test=\"nature != null and nature != ''\">AND nature = #{nature}</if>" +
            "</where>" +
            "</script>")
    Long countByConditions(Map<String, Object> params);

    @Select("SELECT COUNT(*) FROM institution WHERE institution_name LIKE CONCAT('%', #{keyword}, '%')")
    Long countByKeyword(@Param("keyword") String keyword);

    // ========== 志愿方案生成专用查询 ==========

    /**
     * 根据省份查询院校列表
     */
    @Select("SELECT * FROM institution WHERE province_code = #{provinceCode} AND status = 1 AND deleted_at IS NULL")
    List<Institution> selectByProvinceCode(@Param("provinceCode") String provinceCode);

    /**
     * 根据院校等级查询（985/211/双一流/省重点等）
     */
    @Select("SELECT * FROM institution WHERE level = #{level} AND status = 1 AND deleted_at IS NULL")
    List<Institution> selectByLevel(@Param("level") String level);

    /**
     * 根据院校名称精确查询（AI 生成方案时匹配院校 ID）
     */
    @Select("SELECT * FROM institution WHERE institution_name = #{name} LIMIT 1")
    Institution selectByName(@Param("name") String name);

    // 院校自维护字段更新（名称、代码、logo、简介、官网、联系方式、地址）
    @Update("update institution set institution_name=#{institutionName}, institution_code=#{institutionCode}, logo_url=#{logoUrl}, introduction=#{introduction}, official_website=#{officialWebsite}, contact_phone=#{contactPhone}, contact_email=#{contactEmail}, address=#{address}, updated_at=NOW() where id=#{id}")
    Integer updateSelfFields(Institution institution);

    // 院校自维护首次保存（无记录时自动创建占位行）
    @Insert("insert into institution(id, institution_code, institution_name, institution_type, province_code, city_code, address, level, nature, contact_phone, contact_email, official_website, logo_url, introduction, is_online, status, created_at, updated_at) values(#{id}, #{institutionCode}, #{institutionName}, #{institutionType}, #{provinceCode}, #{cityCode}, #{address}, #{level}, #{nature}, #{contactPhone}, #{contactEmail}, #{officialWebsite}, #{logoUrl}, #{introduction}, #{isOnline}, #{status}, #{createdAt}, #{updatedAt})")
    Integer addSelf(Institution institution);

    // 院校自维护首次保存（无ID时，由数据库自动生成ID）
    @Insert("insert into institution(institution_code, institution_name, institution_type, province_code, city_code, address, level, nature, contact_phone, contact_email, official_website, logo_url, introduction, is_online, status, created_at, updated_at) values(#{institutionCode}, #{institutionName}, #{institutionType}, #{provinceCode}, #{cityCode}, #{address}, #{level}, #{nature}, #{contactPhone}, #{contactEmail}, #{officialWebsite}, #{logoUrl}, #{introduction}, #{isOnline}, #{status}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    Integer addSelfAuto(Institution institution);

    // ==================== 管控字段独立更新 ====================

    /** 上下线 */
    @Update("update institution set is_online = #{isOnline} where id = #{id}")
    Integer updateOnlineStatus(@Param("id") Long id, @Param("isOnline") Integer isOnline);

    /** 改 985/211 属性 */
    @Update("update institution set level = #{level} where id = #{id}")
    Integer updateLevel(@Param("id") Long id, @Param("level") String level);

    /** 强制改审核状态 */
    @Update("update institution set status = #{status} where id = #{id}")
    Integer updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
