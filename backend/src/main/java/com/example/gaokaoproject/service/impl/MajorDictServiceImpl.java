package com.example.gaokaoproject.service.impl;

import com.example.gaokaoproject.entity.MajorDict;
import com.example.gaokaoproject.mapper.MajorDictMapper;
import com.example.gaokaoproject.service.MajorDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MajorDictServiceImpl implements MajorDictService {

    @Autowired
    private MajorDictMapper mapper;

    @Override
    public MajorDict getById(Long id) {
        return mapper.getById(id);
    }

    @Override
    public List<MajorDict> list(String majorName) {
        return mapper.list(majorName);
    }

    @Override
    public boolean save(MajorDict dict) {
        return mapper.add(dict) > 0;
    }

    @Override
    public boolean update(MajorDict dict) {
        return mapper.update(dict) > 0;
    }

    @Override
    public boolean delete(Long id) {
        return mapper.del(id) > 0;
    }

    @Override
    public boolean updateOnlineStatus(Long id, Integer status) {
        return mapper.updateOnlineStatus(id, status) > 0;
    }
}
