package com.example.gaokaoproject.controller;

import com.example.gaokaoproject.entity.SubjectDict;
import com.example.gaokaoproject.service.SubjectDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subject")
public class SubjectDictController {

    @Autowired
    private SubjectDictService subjectDictService;

    @GetMapping("/selectAll")
    public List<SubjectDict> selectAll() {
        return subjectDictService.selectSubjectDictAll();
    }

    @GetMapping("/getOne")
    public SubjectDict getOne(@RequestParam Long id) {
        return subjectDictService.selectSubjectDictById(id);
    }

    @PostMapping("/add")
    public Integer add(@RequestBody SubjectDict subjectDict) {
        return subjectDictService.addSubjectDict(subjectDict);
    }

    @PutMapping("/update")
    public Integer update(@RequestBody SubjectDict subjectDict) {
        return subjectDictService.updateSubjectDictOne(subjectDict);
    }

    @DeleteMapping("/delete")
    public Integer delete(@RequestParam Long id) {
        return subjectDictService.deleteSubjectDict(id);
    }
}