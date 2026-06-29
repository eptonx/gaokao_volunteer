package com.example.gaokaoproject.entity; // 保持你的实际包路径不变

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 登录记录实体类 - 对应数据库表login_record
 * 说明：已移除所有MyBatis-Plus(baomidou)相关注解，使用原生MyBatis映射
 */
@Data // Lombok：自动生成getter/setter/toString/equals/hashCode
public class LoginRecord {

    /**
     * 主键ID - 对应数据库自增主键
     * 原生MyBatis通过Mapper配置useGeneratedKeys="true"获取自增ID
     */
    private Long id;

    /**
     * 用户类型 - 对应表中user_type字段（驼峰自动映射）
     */
    private Integer userType;

    /**
     * 用户ID - 对应表中user_id字段
     */
    private Long userId;

    /**
     * 登录类型 - 对应表中login_type字段
     */
    private Integer loginType;

    /**
     * IP地址 - 对应表中ip_address字段
     */
    private String ipAddress;

    /**
     * 设备信息 - 对应表中device_info字段
     */
    private String deviceInfo;

    /**
     * 登录状态 - 对应表中status字段
     */
    private Integer status;

    /**
     * 失败原因 - 对应表中fail_reason字段
     */
    private String failReason;

    /**
     * 创建时间 - 对应表中created_at字段
     */
    private LocalDateTime createdAt;
}