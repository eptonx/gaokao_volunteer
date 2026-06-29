package com.example.gaokaoproject.mapper;

import com.example.gaokaoproject.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

    // 根据手机号查询用户（登录用）
    @Select("SELECT * FROM user WHERE phone = #{phone}")
    User selectByPhone(@Param("phone") String phone);

    // 根据微信openid查询用户
    @Select("SELECT * FROM user WHERE wechat_openid = #{openid}")
    User selectByWechatOpenid(@Param("openid") String openid);

    // 根据ID查询用户
    @Select("SELECT * FROM user WHERE id = #{id}")
    User selectById(@Param("id") Long id);

    // 插入新用户（手机号注册）
    @Insert("INSERT INTO user(phone, password_hash, nickname, avatar_url, wechat_openid, status, created_at, updated_at) " +
            "VALUES(#{phone}, #{passwordHash}, #{nickname}, #{avatarUrl}, #{wechatOpenid}, #{status}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    Integer insertUser(User user);

    // 更新用户信息（昵称、头像等，不含密码）
    @Update("UPDATE user SET phone = #{phone}, nickname = #{nickname}, avatar_url = #{avatarUrl}, updated_at = NOW() " +
            "WHERE id = #{id}")
    Integer updateUser(User user);

    // 仅更新密码
    @Update("UPDATE user SET password_hash = #{passwordHash}, updated_at = NOW() WHERE id = #{id}")
    Integer updatePassword(@Param("id") Long id, @Param("passwordHash") String passwordHash);

    // 删除用户（软删除建议使用 status=0，但这里提供物理删除）
    @Delete("DELETE FROM user WHERE id = #{id}")
    Integer deleteUser(@Param("id") Long id);
}