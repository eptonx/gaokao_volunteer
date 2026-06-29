package com.example.gaokaoproject.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Institution {
    private Long id;
    private String institutionCode;
    private String institutionName;
    private Integer institutionType;
    private String provinceCode;
    private String cityCode;
    private String address;
    private String level;
    private Integer nature;
    private String contactPhone;
    private String contactEmail;
    private String officialWebsite;
    private String logoUrl;
    private String introduction;
    private Integer isOnline;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
