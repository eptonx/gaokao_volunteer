package com.example.gaokaoproject.mapper;

import com.example.gaokaoproject.entity.LoginRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface DashboardMapper {

    // ===== 统计卡片 =====
    @Select("SELECT COUNT(*) FROM user")
    int countUsers();

    @Select("SELECT COUNT(*) FROM institution")
    int countInstitutions();

    @Select("SELECT COUNT(*) FROM volunteer_plan")
    int countPlans();

    @Select("SELECT COUNT(DISTINCT user_id) FROM login_record WHERE DATE(created_at) = CURDATE()")
    int countTodayActive();

    @Select("SELECT COUNT(*) FROM enrollment_plan WHERE status = 1 AND deleted_at IS NULL")
    int countPublishedPlans();

    @Select("SELECT COUNT(*) FROM admission_guide WHERE review_status = 0 AND publish_status = 1 AND deleted_at IS NULL")
    int countPendingGuides();

    @Select("SELECT COUNT(*) FROM institution WHERE is_online = 1 AND status = 1")
    int countOnlineInstitutions();

    @Select("SELECT COUNT(*) FROM user WHERE DATE(created_at) = CURDATE()")
    int countTodayNewUsers();

    // ===== 图表 =====

    @Select("SELECT DATE_FORMAT(created_at, '%m-%d') AS date, COUNT(*) AS count " +
            "FROM login_record WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) " +
            "GROUP BY DATE_FORMAT(created_at, '%m-%d') ORDER BY date")
    List<Map<String, Object>> loginTrends30Days();

    @Select("SELECT i.institution_name AS name, i.level, COUNT(f.id) AS count " +
            "FROM favorite f JOIN institution i ON f.target_id = i.id " +
            "WHERE f.type = 1 GROUP BY f.target_id, i.institution_name, i.level " +
            "ORDER BY FIELD(i.level, '985', '211', '双一流', '普通'), count DESC LIMIT 10")
    List<Map<String, Object>> top10Institutions();

    @Select("SELECT ep.province_code AS province, COUNT(*) AS count " +
            "FROM enrollment_plan ep WHERE ep.deleted_at IS NULL " +
            "GROUP BY ep.province_code ORDER BY count DESC")
    List<Map<String, Object>> provinceDistribution();

    @Select("SELECT level, COUNT(*) AS count FROM institution WHERE is_online = 1 " +
            "GROUP BY level ORDER BY FIELD(level, '985', '211', '双一流', '普通')")
    List<Map<String, Object>> institutionLevelDistribution();

    @Select("SELECT review_status AS status, COUNT(*) AS count FROM admission_guide " +
            "WHERE publish_status = 1 AND deleted_at IS NULL GROUP BY review_status")
    List<Map<String, Object>> reviewStatusOverview();

    @Select("SELECT lr.* FROM login_record lr ORDER BY lr.created_at DESC LIMIT 10")
    List<LoginRecord> recentLogins();
}
