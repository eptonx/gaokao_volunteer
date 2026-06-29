package com.example.gaokaoproject.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Admin implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;                // 管理员ID
    private String username;        // 用户名
    private String passwordHash;    // 密码哈希
    private String realName;        // 真实姓名
    private Long roleId;            // 角色ID
    private Integer status;         // 0-禁用 1-正常
    private String lastLoginIp;     // 最后登录IP
    private LocalDateTime lastLoginAt; // 最后登录时间
    private LocalDateTime createdAt;    // 创建时间
}

