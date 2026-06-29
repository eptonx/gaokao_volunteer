package com.example.gaokaoproject.service.impl;


import com.example.gaokaoproject.entity.Admin;
import com.example.gaokaoproject.mapper.AdminMapper;
import com.example.gaokaoproject.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import com.example.gaokaoproject.exception.ServiceException;
import com.github.pagehelper.PageHelper;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Override
    public List<Admin> selectAll() {
        return adminMapper.selectAll(null);
    }

    @Override
    public List<Admin> selectByPage(int pageNum, int pageSize, String keyword) {
        PageHelper.startPage(pageNum, pageSize);
        return adminMapper.selectAll(keyword);
    }

    @Override
    public Admin selectById(Long id) {
        return adminMapper.selectById(id);
    }

    @Override
    public Integer add(Admin admin) {
        return adminMapper.add(admin);
    }

    @Override
    public Integer update(Admin admin) {
        return adminMapper.updateOne(admin);
    }

    @Override
    public Integer delete(Long id) {
        return adminMapper.delete(id);
    }

    @Override
    public Integer updateStatus(Long id, Integer status) {
        Admin admin = adminMapper.selectById(id);
        if (admin == null) {
            throw new ServiceException(400, "管理员不存在");
        }
        admin.setStatus(status);
        return adminMapper.updateOne(admin);
    }

    @Override
    public Integer updateRoleId(Long id, Long roleId) {
        Admin admin = adminMapper.selectById(id);
        if (admin == null) {
            throw new ServiceException(400, "管理员不存在");
        }
        admin.setRoleId(roleId);
        return adminMapper.updateOne(admin);
    }

    @Override
    public Admin login(String username, String password, String clientIp) {
        // 1、查询用户
        Admin admin = adminMapper.selectByUsername(username);
        // 用户名不存在抛异常
        if (admin == null) {
            throw new ServiceException(20001, "用户名不存在");
        }
        // 2、校验账号状态
        if (admin.getStatus() == 0) {
            throw new ServiceException(20002, "账号已被禁用");
        }
        // 3、比对密码（开发阶段暂用明文比对，后续换 PwdUtil.match）
        if (!password.equals(admin.getPasswordHash())) {
            throw new ServiceException(20003, "密码错误");
        }
        // 4、校验通过，更新登录IP和时间
        adminMapper.updateLoginInfo(admin.getId(), clientIp);
        return admin;
    }

    // 保留你原本的根据用户名查询方法（给鉴权只读用）
    @Override
    public Admin selectByUsername(String username) {
        return adminMapper.selectByUsername(username);
    }

    @Override
    public Integer updatePwd(Long adminId, String oldPwd, String newPwd) {
        // 1、查询当前管理员
        Admin admin = adminMapper.selectById(adminId);
        if (admin == null) {
            throw new ServiceException(400, "管理员不存在");
        }
        // 2、校验旧密码（开发阶段明文比对）
        if (!oldPwd.equals(admin.getPasswordHash())) {
            throw new ServiceException(400, "原密码输入错误");
        }
        // 3、简单校验新密码长度
        if (newPwd.length() < 6) {
            throw new ServiceException(400, "新密码长度不能少于6位");
        }
        // 4、执行更新（开发阶段暂存明文）
        return adminMapper.updatePassword(adminId, newPwd);
    }

}
