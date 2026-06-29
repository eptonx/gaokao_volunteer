package com.example.gaokaoproject.service;

import com.example.gaokaoproject.entity.InstitutionUser;
import java.util.List;

public interface InstitutionUserService {
    List<InstitutionUser> selectAll();
    InstitutionUser selectById(Long id);
    Integer update(InstitutionUser institutionUser);
    Integer add(InstitutionUser institutionUser);
    Integer delete(Long id);

    // 注册（含用户名重复检查，返回含ID的完整用户对象）
    InstitutionUser register(InstitutionUser user);
    // 登录（校验密码，更新登录时间/IP）
    InstitutionUser login(String username, String password, String ip);
    // 改密码
    Integer changePassword(Long id, String oldPassword, String newPassword);
    // 登出（更新最后登录时间）
    Integer logout(Long id);
}