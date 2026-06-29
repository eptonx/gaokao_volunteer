package com.example.gaokaoproject.service.impl;

import com.example.gaokaoproject.entity.Institution;
import com.example.gaokaoproject.mapper.InstitutionMapper;
import com.example.gaokaoproject.service.StudentInstitutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 考生端院校服务实现类
 */
@Service
public class StudentInstitutionServiceImpl implements StudentInstitutionService {

    @Autowired
    private InstitutionMapper institutionMapper;

    @Override
    public List<Institution> selectByConditions(Map<String, Object> params, Integer pageNum, Integer pageSize) {
        // 简单实现，实际应根据条件构建查询语句
        int offset = (pageNum - 1) * pageSize;
        return institutionMapper.selectByConditions(params, offset, pageSize);
    }

    @Override
    public List<Institution> searchByKeyword(String keyword, Integer pageNum, Integer pageSize) {
        // 简单实现，实际应根据关键词构建查询语句
        int offset = (pageNum - 1) * pageSize;
        return institutionMapper.searchByKeyword(keyword, offset, pageSize);
    }

    @Override
    public Map<String, List<String>> getFilterOptions() {
        // 简单实现，实际应从数据库获取
        Map<String, List<String>> options = new HashMap<>();

        // 省份选项
        List<String> provinces = Arrays.asList("北京市", "天津市", "河北省", "山西省", "内蒙古自治区", 
                "辽宁省", "吉林省", "黑龙江省", "上海市", "江苏省", "浙江省", "安徽省", 
                "福建省", "江西省", "山东省", "河南省", "湖北省", "湖南省", "广东省", 
                "广西壮族自治区", "海南省", "重庆市", "四川省", "贵州省", "云南省", 
                "西藏自治区", "陕西省", "甘肃省", "青海省", "宁夏回族自治区", "新疆维吾尔自治区");
        options.put("province", provinces);

        // 层次选项（985/211/双一流/普通）
        List<String> levels = Arrays.asList("985", "211", "双一流", "普通");
        options.put("level", levels);

        // 性质选项
        List<String> properties = Arrays.asList("公办", "民办", "独立学院", "中外合作");
        options.put("property", properties);

        // 科类选项
        List<String> subjects = Arrays.asList("文科", "理科", "综合", "艺术", "体育");
        options.put("subject", subjects);

        return options;
    }

    @Override
    public Long countByConditions(Map<String, Object> params) {
        return institutionMapper.countByConditions(params);
    }

    @Override
    public Long countByKeyword(String keyword) {
        return institutionMapper.countByKeyword(keyword);
    }
}
