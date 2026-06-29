package com.example.gaokaoproject.service.impl;

import com.example.gaokaoproject.entity.StudentScore;
import com.example.gaokaoproject.mapper.StudentScoreMapper;
import com.example.gaokaoproject.service.StudentScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentScoreServiceImpl implements StudentScoreService {

    @Autowired
    private StudentScoreMapper studentScoreMapper;

    @Override
    public List<StudentScore> selectByUserId(Long userId) {
        return studentScoreMapper.selectByUserId(userId);
    }

    @Override
    public StudentScore selectById(Long id) {
        return studentScoreMapper.selectById(id);
    }

    @Override
    public Integer insertStudentScore(StudentScore score) {
        return studentScoreMapper.insertStudentScore(score);
    }

    @Override
    public Integer updateStudentScore(StudentScore score) {
        return studentScoreMapper.updateStudentScore(score);
    }

    @Override
    public Integer deleteStudentScore(Long id) {
        return studentScoreMapper.deleteStudentScore(id);
    }
}