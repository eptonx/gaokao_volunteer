package com.example.gaokaoproject.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {//自定义 SpringSecurity 全局安全配置类。

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())                    // 关闭 CSRF，开发阶段不校验
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()                     // 所有请求放行，鉴权由 LoginInterceptor 处理
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)  // 保留 session，用于存放登录态
            );
        return http.build();
    }
}
