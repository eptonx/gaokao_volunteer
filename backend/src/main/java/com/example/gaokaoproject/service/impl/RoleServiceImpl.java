package com.example.gaokaoproject.service.impl;

import com.example.gaokaoproject.entity.Role;
import com.example.gaokaoproject.mapper.RoleMapper;
import com.example.gaokaoproject.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public List<Role> selectAll() {
        return roleMapper.selectAll();
    }

    @Override
    public Role selectById(Long id) {
        return roleMapper.selectById(id);
    }

    @Override
    public Integer add(Role role) {
        return roleMapper.add(role);
    }

    @Override
    public Integer update(Role role) {
        return roleMapper.updateOne(role);
    }

    @Override
    public Integer updateStatus(Long id, Integer status) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }
        return roleMapper.updateStatus(id, status);
    }

    @Override
    public Integer delete(Long id) {
        return roleMapper.delete(id);
    }
}