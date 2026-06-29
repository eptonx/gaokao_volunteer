package com.example.gaokaoproject.mapper;

import com.example.gaokaoproject.entity.Role;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface RoleMapper {

    // 查询所有角色
    @Select("SELECT * FROM role")
    List<Role> selectAll();

    // 根据id查询单个角色
    @Select("SELECT * FROM role WHERE id = #{id}")
    Role selectById(Long id);

    // 新增角色（created_at数据库自动填充，不插入）
    @Insert("INSERT INTO role(name,code,description,status) VALUES(#{name},#{code},#{description},#{status})")
    Integer add(Role role);

    // 修改角色信息
    @Update("UPDATE role SET name=#{name},code=#{code},description=#{description},status=#{status} WHERE id=#{id}")
    Integer updateOne(Role role);

    // 启用/禁用角色
    @Update("UPDATE role SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    // 根据id删除角色
    @Delete("DELETE FROM role WHERE id = #{id}")
    Integer delete(Long id);
}