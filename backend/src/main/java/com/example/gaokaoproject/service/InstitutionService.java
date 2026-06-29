package com.example.gaokaoproject.service;

import com.example.gaokaoproject.entity.Institution;

import java.util.List;

public interface InstitutionService {
    Integer add(Institution institution);
    Integer update(Institution institution);
    Integer delete(Long id);
    List<Institution> selectAll();
    Institution selectById(Long id);
    Institution selectByName(String name);
    // 院校自维护字段更新（名称、代码、logo、简介、官网、联系方式、地址），返回最新的院校对象
    Institution updateSelfFields(Institution institution);

    // 运营端管控
    Integer updateOnlineStatus(Long id, Integer isOnline);
    Integer updateLevel(Long id, String level);
    Integer updateStatus(Long id, Integer status);
}
