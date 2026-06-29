package com.example.gaokaoproject.service;

import com.example.gaokaoproject.entity.Role;
import java.util.List;

public interface RoleService {
    List<Role> selectAll();
    Role selectById(Long id);
    Integer add(Role role);
    Integer update(Role role);
    Integer updateStatus(Long id, Integer status);
    Integer delete(Long id);
}