package com.example.gaokaoproject.service;

import com.example.gaokaoproject.entity.User;
import java.util.List;

public interface UserService {
    User selectByPhone(String phone);
    User selectByWechatOpenid(String openid);
    User selectById(Long id);
    Integer insertUser(User user);
    Integer updateUser(User user);
    Integer updatePassword(Long id, String passwordHash);
    Integer deleteUser(Long id);
}