package com.example.gaokaoproject.entity;

import lombok.Data;

@Data
public class Region {
    private Long id;
    private String code;
    private String name;
    private Integer level;      // 1-省 2-市
    private Long parentId;      // 父级ID，省级为0
}