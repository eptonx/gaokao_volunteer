package com.example.gaokaoproject.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Role {
    private Long id;                // 角色ID
    private String name;            // 角色名称
    private String code;            // 角色编码
    private String description;     // 描述
    private Integer status;         // 0-禁用 1-正常
    private LocalDateTime createdAt; // 创建时间
}