package com.example.gaokaoproject.mapper;

import com.example.gaokaoproject.entity.Region;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RegionMapper {

    @Select("SELECT id, code, name, level, parent_id as parentId FROM region")
    List<Region> selectAll();

    @Select("SELECT id, code, name, level, parent_id as parentId FROM region WHERE id = #{id}")
    Region selectById(Long id);

    @Insert("INSERT INTO region(code, name, level, parent_id) VALUES(#{code}, #{name}, #{level}, #{parentId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    Integer add(Region region);

    @Update("UPDATE region SET code = #{code}, name = #{name}, level = #{level}, parent_id = #{parentId} WHERE id = #{id}")
    Integer updateOne(Region region);

    @Delete("DELETE FROM region WHERE id = #{id}")
    Integer delete(Long id);
}