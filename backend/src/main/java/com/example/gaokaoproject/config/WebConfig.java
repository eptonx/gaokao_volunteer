package com.example.gaokaoproject.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/admin/**", "/qualification/**")  // 只拦截管理员和审核接口
                .excludePathPatterns("/admin/login", "/admin/logout",
                        "/qualification/byInstitution", "/qualification/add");  // 登录登出 + 院校端自查询放行
    }
}