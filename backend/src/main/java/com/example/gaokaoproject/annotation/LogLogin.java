package com.example.gaokaoproject.annotation;

import java.lang.annotation.*;

/**
 * 登录日志注解 —— 标记在登录接口上，AOP 自动写入 login_record
 *
 * 用法：@LogLogin(userType = 3)  // 1=考生, 2=院校, 3=管理员
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogLogin {
    int userType() default 1;
}

