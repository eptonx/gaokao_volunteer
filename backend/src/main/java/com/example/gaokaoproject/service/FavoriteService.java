package com.example.gaokaoproject.service;

import com.example.gaokaoproject.entity.Favorite;

import java.util.List;

public interface FavoriteService {
    /**
     * 添加收藏
     * @param favorite 收藏信息
     * @return 操作结果
     */
    Integer add(Favorite favorite);
    
    /**
     * 更新收藏
     * @param favorite 收藏信息
     * @return 操作结果
     */
    Integer update(Favorite favorite);
    
    /**
     * 删除收藏
     * @param id 收藏ID
     * @return 操作结果
     */
    Integer delete(Long id);
    
    /**
     * 根据用户和目标删除收藏
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param type 收藏类型
     * @return 操作结果
     */
    Integer deleteByUserAndTarget(Long userId, Long targetId, Integer type);
    
    /**
     * 查询所有收藏
     * @return 收藏列表
     */
    List<Favorite> selectAll();
    
    /**
     * 根据ID查询收藏
     * @param id 收藏ID
     * @return 收藏信息
     */
    Favorite selectById(Long id);
    
    /**
     * 获取用户的收藏列表
     * @param userId 用户ID
     * @param type 收藏类型（1:院校, 2:专业）
     * @return 收藏列表
     */
    List<Favorite> selectByUserId(Long userId, Integer type);

    /**
     * 获取用户的所有收藏（不区分类型）
     * @param userId 用户ID
     * @return 收藏列表
     */
    List<Favorite> selectByUserId(Long userId);
    
    /**
     * 检查是否已收藏
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param type 收藏类型
     * @return 收藏数量
     */
    Integer countByUserAndTarget(Long userId, Long targetId, Integer type);
}
