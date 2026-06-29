package com.example.gaokaoproject.mapper;

import com.example.gaokaoproject.entity.StudentScore;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface StudentScoreMapper {

    // 根据用户ID查询成绩（可能有历史年份，取最新或全部）
    @Select("SELECT * FROM student_score WHERE user_id = #{userId} ORDER BY exam_year DESC")
    List<StudentScore> selectByUserId(@Param("userId") Long userId);

    // 根据ID查询单条成绩
    @Select("SELECT * FROM student_score WHERE id = #{id}")
    StudentScore selectById(@Param("id") Long id);

    // 插入成绩记录（注意字段映射）
    @Insert("INSERT INTO student_score(user_id, province_code, exam_year, subject_type, selected_subjects, " +
            "total_score, province_rank, equivalent_score, created_at, updated_at) " +
            "VALUES(#{userId}, #{provinceCode}, #{examYear}, #{subjectType}, #{selectedSubjects}, " +
            "#{totalScore}, #{provinceRank}, #{equivalentScore}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    Integer insertStudentScore(StudentScore score);

    // 更新成绩记录（更新时间自动）
    @Update("UPDATE student_score SET province_code = #{provinceCode}, exam_year = #{examYear}, " +
            "subject_type = #{subjectType}, selected_subjects = #{selectedSubjects}, " +
            "total_score = #{totalScore}, province_rank = #{provinceRank}, " +
            "equivalent_score = #{equivalentScore}, updated_at = NOW() " +
            "WHERE id = #{id}")
    Integer updateStudentScore(StudentScore score);

    // 删除成绩记录
    @Delete("DELETE FROM student_score WHERE id = #{id}")
    Integer deleteStudentScore(@Param("id") Long id);
}