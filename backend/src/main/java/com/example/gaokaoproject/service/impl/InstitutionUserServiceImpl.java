package com.example.gaokaoproject.service.impl;

import com.example.gaokaoproject.entity.InstitutionUser;
import com.example.gaokaoproject.mapper.InstitutionUserMapper;
import com.example.gaokaoproject.service.InstitutionUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InstitutionUserServiceImpl implements InstitutionUserService {

    @Autowired
    private InstitutionUserMapper institutionUserMapper;

    @Override
    public List<InstitutionUser> selectAll() {
        return institutionUserMapper.selectAll();
    }

    @Override
    public InstitutionUser selectById(Long id) {
        return institutionUserMapper.selectById(id);
    }

    @Override
    public Integer update(InstitutionUser institutionUser) {
        return institutionUserMapper.update(institutionUser);
    }

    @Override
    public Integer add(InstitutionUser institutionUser) {
        return institutionUserMapper.add(institutionUser);
    }

    @Override
    public Integer delete(Long id) {
        return institutionUserMapper.delete(id);
    }

    // ========== 新增业务方法 ==========

    @Override
    public InstitutionUser register(InstitutionUser user) {
        // 检查用户名是否已存在
        InstitutionUser exist = institutionUserMapper.selectByUsername(user.getUsername());
        if (exist != null) {
            throw new RuntimeException("用户名已存在");
        }
        // 补齐必填默认值，避免数据库报错
        if (user.getStatus() == null) user.setStatus(1);
        if (user.getInstitutionId() == null) user.setInstitutionId(0L);
        if (user.getEmail() == null || user.getEmail().isEmpty()) user.setEmail("");
        if (user.getLastLoginIp() == null) user.setLastLoginIp("");
        institutionUserMapper.add(user);  // @Options 会自动回填 id
        user.setPasswordHash(null);       // 不返回密码
        return user;
    }

    @Override
    public InstitutionUser login(String username, String password, String ip) {
        InstitutionUser user = institutionUserMapper.selectByUsername(username);
        if (user == null) {
            System.out.println("登录失败：用户名不存在 - " + username);
            throw new RuntimeException("用户名或密码错误");
        }
        if (!password.equals(user.getPasswordHash())) {
            System.out.println("登录失败：密码错误 - 用户=" + username + " 输入=" + password + " 库中=" + user.getPasswordHash());
            throw new RuntimeException("用户名或密码错误");
        }
        System.out.println("登录成功：用户=" + username + " institutionId=" + user.getInstitutionId());
        // 更新登录时间/IP
        institutionUserMapper.updateLoginInfo(user.getId(), LocalDateTime.now(), ip);
        // 不返回密码
        user.setPasswordHash(null);
        return user;
    }

    @Override
    public Integer changePassword(Long id, String oldPassword, String newPassword) {
        InstitutionUser user = institutionUserMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!oldPassword.equals(user.getPasswordHash())) {
            throw new RuntimeException("原密码错误");
        }
        return institutionUserMapper.updatePassword(id, newPassword);
    }

    @Override
    public Integer logout(Long id) {
        return institutionUserMapper.updateLoginInfo(id, LocalDateTime.now(), null);
    }
}