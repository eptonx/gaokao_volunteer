package com.example.gaokaoproject.aspect;

import com.example.gaokaoproject.annotation.LogLogin;
import com.example.gaokaoproject.entity.LoginRecord;
import com.example.gaokaoproject.mapper.LoginRecordMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 登录日志 AOP —— 拦截 @LogLogin 注解的方法，自动写入 login_record
 */
@Aspect
@Component
public class LoginAspect {

    @Autowired
    private LoginRecordMapper loginRecordMapper;

    @Around("@annotation(logLogin)")
    public Object logLoginRecord(ProceedingJoinPoint joinPoint, LogLogin logLogin) throws Throwable {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String ip = "unknown";
        String device = "unknown";

        if (attributes != null) {
            HttpServletRequest req = attributes.getRequest();
            ip = getClientIp(req);
            String ua = req.getHeader("User-Agent");
            if (ua != null) {
                device = ua.length() > 255 ? ua.substring(0, 255) : ua;
            }
        }

        Object[] args = joinPoint.getArgs();
        Long userId = extractUserId(args, attributes);

        LoginRecord record = new LoginRecord();
        record.setUserType(logLogin.userType());
        record.setUserId(userId);
        record.setIpAddress(ip);
        record.setDeviceInfo(device);
        record.setLoginType(1); // 1=密码登录
        record.setCreatedAt(LocalDateTime.now());

        Object result;
        try {
            result = joinPoint.proceed();
            record.setStatus(1); // 1=成功
            if (userId == null && result != null) {
                userId = extractUserIdFromResult(result);
                record.setUserId(userId);
            }
        } catch (Throwable e) {
            record.setStatus(0); // 0=失败
            String reason = e.getMessage();
            if (reason != null) {
                record.setFailReason(reason.length() > 255 ? reason.substring(0, 255) : reason);
            }
            throw e;
        } finally {
            try {
                loginRecordMapper.insert(record);
            } catch (Exception ignored) {}
        }
        return result;
    }

    private Long extractUserId(Object[] args, ServletRequestAttributes attributes) {
        if (args == null) return null;
        for (Object arg : args) {
            if (arg instanceof Long l) return l;
        }
        if (attributes != null) {
            String p = attributes.getRequest().getParameter("userId");
            if (p != null) {
                try { return Long.valueOf(p); } catch (NumberFormatException ignored) {}
            }
        }
        return null;
    }

    private Long extractUserIdFromResult(Object result) {
        try {
            return (Long) result.getClass().getMethod("getId").invoke(result);
        } catch (Exception e) {
            return null;
        }
    }

    private String getClientIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
