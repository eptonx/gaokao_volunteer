package com.example.gaokaoproject.mapper;

import com.example.gaokaoproject.entity.MajorDict;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MajorDictMapper {

    @Select("select * from major_dict where id = #{id}")
    MajorDict getById(Long id);

    @Select("select * from major_dict where name like concat('%',#{majorName},'%')")
    List<MajorDict> list(String majorName);

    @Insert("insert into major_dict (code, name, category, sub_category, description, status, created_at, updated_at) " +
            "values (#{code}, #{name}, #{category}, #{subCategory}, #{description}, #{status}, #{createdAt}, #{updatedAt})")
    int add(MajorDict dict);

    @Update("update major_dict set code = #{code}, name = #{name}, category = #{category}, sub_category = #{subCategory}, " +
            "description = #{description}, status = #{status}, updated_at = #{updatedAt} where id = #{id}")
    int update(MajorDict dict);

    @Delete("delete from major_dict where id = #{id}")
    int del(Long id);

    /** 只更新上下线状态 */
    @Update("update major_dict set status = #{status} where id = #{id}")
    int updateOnlineStatus(@Param("id") Long id, @Param("status") Integer status);
}
