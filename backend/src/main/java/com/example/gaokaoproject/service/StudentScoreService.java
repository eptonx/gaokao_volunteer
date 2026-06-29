package com.example.gaokaoproject.service;

import com.example.gaokaoproject.entity.StudentScore;
import java.util.List;

public interface StudentScoreService {
    List<StudentScore> selectByUserId(Long userId);
    StudentScore selectById(Long id);
    Integer insertStudentScore(StudentScore score);
    Integer updateStudentScore(StudentScore score);
    Integer deleteStudentScore(Long id);
}