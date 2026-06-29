package com.example.gaokaoproject.service.impl;

import com.example.gaokaoproject.entity.DataImportLog;
import com.example.gaokaoproject.mapper.DataImportLogMapper;
import com.example.gaokaoproject.service.DataImportLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataImportLogServiceImpl implements DataImportLogService {

    @Autowired
    private DataImportLogMapper mapper;

    @Override
    public List<DataImportLog> selectAll() {
        return mapper.selectAll();
    }

    @Override
    public DataImportLog selectById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public boolean add(DataImportLog log) {
        return mapper.add(log) > 0;
    }

    @Override
    public boolean update(DataImportLog log) {
        return mapper.update(log) > 0;
    }

    @Override
    public boolean delete(Long id) {
        return mapper.delete(id) > 0;
    }
}