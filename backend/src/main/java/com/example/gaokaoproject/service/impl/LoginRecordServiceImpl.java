package com.example.gaokaoproject.service.impl;

import com.example.gaokaoproject.entity.LoginRecord;
import com.example.gaokaoproject.mapper.LoginRecordMapper;
import com.example.gaokaoproject.service.LoginRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service  // 标记为Spring服务组件，自动被Spring管理
@Transactional  // 事务管理：增删改自动回滚异常
public class LoginRecordServiceImpl implements LoginRecordService {

    @Autowired  // 自动注入Mapper，直接调用数据库操作
    private LoginRecordMapper loginRecordMapper;

    // 新增
    @Override
    public int addLoginRecord(LoginRecord loginRecord) {
        return loginRecordMapper.insert(loginRecord);
    }

    // 删除
    @Override
    public int deleteLoginRecordById(Long id) {
        return loginRecordMapper.deleteById(id);
    }

    // 更新
    @Override
    public int updateLoginRecordById(LoginRecord loginRecord) {
        return loginRecordMapper.updateById(loginRecord);
    }

    // 查单条（查询不需要事务，设为只读优化性能）
    @Override
    @Transactional(readOnly = true)
    public LoginRecord getLoginRecordById(Long id) {
        return loginRecordMapper.selectById(id);
    }

    // 查所有（只读优化）
    @Override
    @Transactional(readOnly = true)
    public List<LoginRecord> getAllLoginRecords() {
        return loginRecordMapper.selectAll();
    }

    // 按用户ID查登录记录（只读优化，时间倒序）
    @Override
    @Transactional(readOnly = true)
    public List<LoginRecord> getLoginRecordsByUserId(Long userId) {
        return loginRecordMapper.selectByUserId(userId);
    }

    // === M6 分页查询 ===
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> pageList(LoginRecord query, int offset, int pageSize, String startTime, String endTime) {
        int total = loginRecordMapper.countByCondition(query, startTime, endTime);
        List<LoginRecord> records = loginRecordMapper.listByCondition(query, offset, pageSize, startTime, endTime);
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", records);
        result.put("pageNum", (offset / pageSize) + 1);
        result.put("pageSize", pageSize);
        return result;
    }
}