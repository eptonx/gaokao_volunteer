package com.example.gaokaoproject.mapper;

import com.example.gaokaoproject.entity.Admin;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface AdminMapper {

    // 查询全部管理员（支持关键词搜索）
    @Select("<script>" +
            "SELECT * FROM admin" +
            "<where>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (username LIKE CONCAT('%',#{keyword},'%') OR real_name LIKE CONCAT('%',#{keyword},'%'))" +
            "</if>" +
            "</where>" +
            "</script>")
    List<Admin> selectAll(@Param("keyword") String keyword);

    // 根据主键id单查
    @Select("SELECT * FROM admin WHERE id = #{id}")
    Admin selectById(Long id);

    // 新增管理员（created_at数据库自动填充，不写入insert语句）
    @Insert("INSERT INTO admin(username, password_hash, real_name, role_id, status, last_login_ip, last_login_at) " +
            "VALUES(#{username},#{passwordHash},#{realName},#{roleId},#{status},#{lastLoginIp},#{lastLoginAt})")
    Integer add(Admin admin);

    // 修改全部可编辑字段
    @Update("UPDATE admin SET username=#{username},password_hash=#{passwordHash},real_name=#{realName}," +
            "role_id=#{roleId},status=#{status},last_login_ip=#{lastLoginIp},last_login_at=#{lastLoginAt} WHERE id=#{id}")
    Integer updateOne(Admin admin);

    // 根据id删除管理员
    @Delete("DELETE FROM admin WHERE id = #{id}")
    Integer delete(Long id);

    // 登录鉴权专用：根据用户名查询（M1登录校验使用）
    @Select("SELECT * FROM admin WHERE username = #{username}")
    Admin selectByUsername(String username);

    // 登录成功后更新最后登录IP、登录时间
    @Update("UPDATE admin SET last_login_ip = #{ip}, last_login_at = NOW() WHERE id = #{adminId}")
    void updateLoginInfo(@Param("adminId") Long adminId, @Param("ip") String ip);

    // 修改密码
    @Update("UPDATE admin SET password_hash = #{newPwdHash} WHERE id = #{adminId}")
    int updatePassword(@Param("adminId") Long adminId, @Param("newPwdHash") String newPwdHash);
}
