package com.example.gaokaoproject.service;

import com.example.gaokaoproject.entity.DataImportLog;
import java.util.List;

public interface DataImportLogService {
    List<DataImportLog> selectAll();
    DataImportLog selectById(Long id);
    boolean add(DataImportLog log);
    boolean update(DataImportLog log);
    boolean delete(Long id);
}