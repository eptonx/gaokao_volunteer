package com.example.gaokaoproject.service;

import com.example.gaokaoproject.entity.Admin;
import java.util.List;

public interface AdminService {
    List<Admin> selectAll();
    List<Admin> selectByPage(int pageNum, int pageSize, String keyword);
    Admin selectById(Long id);
    Integer add(Admin admin);
    Integer update(Admin admin);
    Integer delete(Long id);
    Admin selectByUsername(String username);

    // 启用/禁用
    Integer updateStatus(Long id, Integer status);

    // 分配角色
    Integer updateRoleId(Long id, Long roleId);

    // 完整登录业务方法
    Admin login(String username, String password, String clientIp);

    // 修改登录密码
    Integer updatePwd(Long adminId, String oldPwd, String newPwd);
}