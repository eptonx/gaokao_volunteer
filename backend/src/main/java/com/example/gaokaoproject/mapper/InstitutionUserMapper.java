package com.example.gaokaoproject.mapper;
import com.example.gaokaoproject.entity.InstitutionUser;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface InstitutionUserMapper {
    @Select("select id,institution_id,username,password_hash,real_name,mobile,email,last_login_at,last_login_ip,status,created_at from institution_user")
    List<InstitutionUser> selectAll();

    @Select("select id,institution_id,username,password_hash,real_name,mobile,email,last_login_at,last_login_ip,status,created_at from institution_user where id = #{id}")
    InstitutionUser selectById(Long id);

    @Update("update institution_user set institution_id=#{institutionId},username=#{username},password_hash=#{passwordHash},real_name=#{realName},mobile=#{mobile},email=#{email},last_login_at=#{lastLoginAt},last_login_ip=#{lastLoginIp},status=#{status} where id=#{id}")
    Integer update(InstitutionUser institutionUser);

    @Insert("insert into institution_user(institution_id,username,password_hash,real_name,mobile,email,last_login_at,last_login_ip,status,created_at) values(#{institutionId},#{username},#{passwordHash},#{realName},#{mobile},#{email},#{lastLoginAt},#{lastLoginIp},#{status},NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    Integer add(InstitutionUser institutionUser);

    @Delete("delete from institution_user where id = #{id}")
    Integer delete(Long id);

    // 登录：按用户名查询
    @Select("select id,institution_id,username,password_hash,real_name,mobile,email,last_login_at,last_login_ip,status,created_at from institution_user where username = #{username}")
    InstitutionUser selectByUsername(String username);

    // 改密码
    @Update("update institution_user set password_hash = #{passwordHash} where id = #{id}")
    Integer updatePassword(@Param("id") Long id, @Param("passwordHash") String passwordHash);

    // 登录/登出时更新登录时间和IP
    @Update("update institution_user set last_login_at = #{lastLoginAt}, last_login_ip = #{lastLoginIp} where id = #{id}")
    Integer updateLoginInfo(@Param("id") Long id, @Param("lastLoginAt") java.time.LocalDateTime lastLoginAt, @Param("lastLoginIp") String lastLoginIp);
}