package com.example.gaokaoproject.service;

import java.util.Map;

public interface WorkbenchService {
    // 首页数据概览
    Map<String, Object> overview(Long institutionId);

    // 数据迁移：把 fromId 的数据批量迁到 toId
    Map<String, Object> migrateData(Long fromId, Long toId);
}
