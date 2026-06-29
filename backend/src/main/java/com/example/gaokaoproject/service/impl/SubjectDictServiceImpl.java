package com.example.gaokaoproject.service.impl;

import com.example.gaokaoproject.entity.SubjectDict;
import com.example.gaokaoproject.mapper.SubjectDictMapper;
import com.example.gaokaoproject.service.SubjectDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubjectDictServiceImpl implements SubjectDictService {

    @Autowired
    private SubjectDictMapper subjectDictMapper;

    @Override
    public List<SubjectDict> selectSubjectDictAll() {
        return subjectDictMapper.selectAll();
    }

    @Override
    public SubjectDict selectSubjectDictById(Long id) {
        return subjectDictMapper.selectById(id);
    }

    @Override
    public Integer updateSubjectDictOne(SubjectDict subjectDict) {
        return subjectDictMapper.updateOne(subjectDict);
    }

    @Override
    public Integer addSubjectDict(SubjectDict subjectDict) {
        return subjectDictMapper.add(subjectDict);
    }

    @Override
    public Integer deleteSubjectDict(Long id) {
        return subjectDictMapper.delete(id);
    }
}