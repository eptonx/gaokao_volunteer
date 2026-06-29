package com.example.gaokaoproject.mapper;

import com.example.gaokaoproject.entity.LoginRecord;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface LoginRecordMapper {

    // 1. 新增：添加登录记录（已修复created_at问题，数据库自动填充）
    @Insert("INSERT INTO login_record(user_type, user_id, login_type, ip_address, device_info, status, fail_reason) " +
            "VALUES(#{userType}, #{userId}, #{loginType}, #{ipAddress}, #{deviceInfo}, #{status}, #{failReason})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(LoginRecord loginRecord);

    // 2. 删除：根据ID删除（无需修改）
    @Delete("DELETE FROM login_record WHERE id=#{id}")
    int deleteById(Long id);

    // 3. 修改：根据ID更新（无需修改）
    @Update("UPDATE login_record SET user_type=#{userType}, user_id=#{userId}, login_type=#{loginType}, " +
            "ip_address=#{ipAddress}, device_info=#{deviceInfo}, status=#{status}, fail_reason=#{failReason} WHERE id=#{id}")
    int updateById(LoginRecord loginRecord);

    // 4. 查询：根据ID查单条（无需修改）
    @Select("SELECT * FROM login_record WHERE id=#{id}")
    LoginRecord selectById(Long id);

    // 5. 查询：查所有记录（无需修改）
    @Select("SELECT * FROM login_record")
    List<LoginRecord> selectAll();

    // 6. 查询：根据用户ID查登录记录（按时间倒序）
    @Select("SELECT * FROM login_record WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<LoginRecord> selectByUserId(Long userId);

    // === M6 分页查询 ===
    @Select("<script>" +
            "SELECT COUNT(*) FROM login_record WHERE 1=1" +
            "<if test='query.userId != null'> AND user_id = #{query.userId}</if>" +
            "<if test='query.status != null'> AND status = #{query.status}</if>" +
            "<if test='startTime != null and startTime != \"\"'> AND created_at &gt;= #{startTime}</if>" +
            "<if test='endTime != null and endTime != \"\"'> AND created_at &lt;= #{endTime}</if>" +
            "</script>")
    int countByCondition(@Param("query") LoginRecord query,
                         @Param("startTime") String startTime,
                         @Param("endTime") String endTime);

    @Select("<script>" +
            "SELECT * FROM login_record WHERE 1=1" +
            "<if test='query.userId != null'> AND user_id = #{query.userId}</if>" +
            "<if test='query.status != null'> AND status = #{query.status}</if>" +
            "<if test='startTime != null and startTime != \"\"'> AND created_at &gt;= #{startTime}</if>" +
            "<if test='endTime != null and endTime != \"\"'> AND created_at &lt;= #{endTime}</if>" +
            " ORDER BY created_at DESC LIMIT #{offset}, #{pageSize}" +
            "</script>")
    List<LoginRecord> listByCondition(@Param("query") LoginRecord query,
                                      @Param("offset") int offset,
                                      @Param("pageSize") int pageSize,
                                      @Param("startTime") String startTime,
                                      @Param("endTime") String endTime);
}