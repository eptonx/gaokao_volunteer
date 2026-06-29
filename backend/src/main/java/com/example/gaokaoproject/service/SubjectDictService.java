package com.example.gaokaoproject.service;

import com.example.gaokaoproject.entity.SubjectDict;
import java.util.List;

public interface SubjectDictService {
    List<SubjectDict> selectSubjectDictAll();

    SubjectDict selectSubjectDictById(Long id);

    Integer updateSubjectDictOne(SubjectDict subjectDict);

    Integer addSubjectDict(SubjectDict subjectDict);

    Integer deleteSubjectDict(Long id);
}