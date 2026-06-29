package com.example.gaokaoproject.service.impl;

import com.example.gaokaoproject.dto.AuthResponse;
import com.example.gaokaoproject.dto.LoginRequest;
import com.example.gaokaoproject.dto.RegisterRequest;
import com.example.gaokaoproject.entity.LoginRecord;
import com.example.gaokaoproject.entity.User;
import com.example.gaokaoproject.exception.ServiceException;
import com.example.gaokaoproject.mapper.UserMapper;
import com.example.gaokaoproject.service.AuthService;
import com.example.gaokaoproject.service.LoginRecordService;
import com.example.gaokaoproject.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private LoginRecordService loginRecordService;

    @Override
    public Long register(RegisterRequest request) {
        String phone = request.getPhone();
        String password = request.getPassword();
        String nickname = request.getNickname();

        User existUser = userMapper.selectByPhone(phone);
        if (existUser != null) {
            throw new ServiceException("手机号已被注册");
        }

        User user = new User();
        user.setPhone(phone);
        user.setPasswordHash(password);
        user.setNickname(nickname);
        user.setStatus(1);
        userMapper.insertUser(user);

        return user.getId();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // 获取客户端 IP 和 User-Agent
        String ip = "未知";
        String ua = "未知";
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest httpReq = attrs.getRequest();
                ip = getClientIp(httpReq);
                ua = httpReq.getHeader("User-Agent");
                if (ua == null || ua.isEmpty()) ua = "未知";
            }
        } catch (Exception ignored) {}

        User user = userMapper.selectByPhone(request.getPhone());

        // 账号或密码错误
        if (user == null || !request.getPassword().equals(user.getPasswordHash())) {
            LoginRecord failRecord = new LoginRecord();
            failRecord.setUserType(1);       // 1=普通用户
            failRecord.setUserId(null);       // 查不到用户，userId 为空
            failRecord.setLoginType(1);       // 1=手机号密码登录
            failRecord.setIpAddress(ip);
            failRecord.setDeviceInfo(ua);
            failRecord.setStatus(0);          // 0=失败
            failRecord.setFailReason("手机号或密码错误");
            try { loginRecordService.addLoginRecord(failRecord); } catch (Exception ignored) {}
            throw new ServiceException("手机号或密码错误");
        }

        // 账号被禁用
        if (user.getStatus() != 1) {
            LoginRecord failRecord = new LoginRecord();
            failRecord.setUserType(1);
            failRecord.setUserId(user.getId());
            failRecord.setLoginType(1);
            failRecord.setIpAddress(ip);
            failRecord.setDeviceInfo(ua);
            failRecord.setStatus(0);
            failRecord.setFailReason("账号已被禁用");
            try { loginRecordService.addLoginRecord(failRecord); } catch (Exception ignored) {}
            throw new ServiceException("账号已被禁用");
        }

        // 登录成功
        LoginRecord successRecord = new LoginRecord();
        successRecord.setUserType(1);
        successRecord.setUserId(user.getId());
        successRecord.setLoginType(1);
        successRecord.setIpAddress(ip);
        successRecord.setDeviceInfo(ua);
        successRecord.setStatus(1);           // 1=成功
        try { loginRecordService.addLoginRecord(successRecord); } catch (Exception ignored) {}

        String accessToken = jwtUtil.generateAccessToken(user.getId());
        return new AuthResponse(user.getId(), accessToken, null);
    }

    /**
     * 获取客户端真实 IP（考虑反向代理）
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
