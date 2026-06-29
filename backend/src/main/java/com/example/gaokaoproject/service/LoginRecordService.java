package com.example.gaokaoproject.service;

import com.example.gaokaoproject.entity.LoginRecord;
import java.util.List;

// 定义业务层接口，规范增删改查方法
public interface LoginRecordService {
    // 新增登录记录
    int addLoginRecord(LoginRecord loginRecord);

    // 根据ID删除
    int deleteLoginRecordById(Long id);

    // 根据ID更新
    int updateLoginRecordById(LoginRecord loginRecord);

    // 根据ID查询单条
    LoginRecord getLoginRecordById(Long id);

    // 查询所有记录
    List<LoginRecord> getAllLoginRecords();

    // 根据用户ID查询登录记录（按时间倒序）
    List<LoginRecord> getLoginRecordsByUserId(Long userId);

    // === M6 分页查询 ===
    java.util.Map<String, Object> pageList(LoginRecord query, int offset, int pageSize, String startTime, String endTime);
}