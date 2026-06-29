package com.example.gaokaoproject.service.impl;

import com.example.gaokaoproject.entity.User;
import com.example.gaokaoproject.mapper.UserMapper;
import com.example.gaokaoproject.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User selectByPhone(String phone) {
        return userMapper.selectByPhone(phone);
    }

    @Override
    public User selectByWechatOpenid(String openid) {
        return userMapper.selectByWechatOpenid(openid);
    }

    @Override
    public User selectById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public Integer insertUser(User user) {
        return userMapper.insertUser(user);
    }

    @Override
    public Integer updateUser(User user) {
        return userMapper.updateUser(user);
    }

    @Override
    public Integer updatePassword(Long id, String passwordHash) {
        return userMapper.updatePassword(id, passwordHash);
    }

    @Override
    public Integer deleteUser(Long id) {
        return userMapper.deleteUser(id);
    }

}