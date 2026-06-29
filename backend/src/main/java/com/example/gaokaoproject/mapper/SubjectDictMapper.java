package com.example.gaokaoproject.mapper;

import com.example.gaokaoproject.entity.SubjectDict;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SubjectDictMapper {

    @Select("SELECT id, code, name FROM subject_dict")
    List<SubjectDict> selectAll();

    @Select("SELECT id, code, name FROM subject_dict WHERE id = #{id}")
    SubjectDict selectById(Long id);

    @Insert("INSERT INTO subject_dict(code, name) VALUES(#{code}, #{name})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    Integer add(SubjectDict subjectDict);

    @Update("UPDATE subject_dict SET code = #{code}, name = #{name} WHERE id = #{id}")
    Integer updateOne(SubjectDict subjectDict);

    @Delete("DELETE FROM subject_dict WHERE id = #{id}")
    Integer delete(Long id);
}