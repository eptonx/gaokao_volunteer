package com.example.gaokaoproject.controller;

import com.example.gaokaoproject.entity.Favorite;
import com.example.gaokaoproject.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favorite")
@Tag(name = "收藏管理", description = "收藏管理相关接口")
public class FavoriteController {
    
    @Autowired
    private FavoriteService favoriteService;
    
    /**
     * 添加收藏（院校或专业）
     * @param favorite 收藏信息
     * @return 操作结果
     */
    @Operation(summary = "添加收藏")
    @PostMapping("/add")
    public Integer add(@RequestBody Favorite favorite) {
        favorite.setCreatedAt(null); // 不接收前端传来的时间
        return favoriteService.add(favorite);
    }
    
    /**
     * 取消收藏（院校或专业）
     * @param userId 用户ID
     * @param targetId 目标ID（院校ID或专业ID）
     * @param type 收藏类型（1:院校, 2:专业）
     * @return 操作结果
     */
    @Operation(summary = "取消收藏")
    @DeleteMapping("/cancel")
    public Integer cancel(@RequestParam Long userId, @RequestParam Long targetId, @RequestParam Integer type) {
        return favoriteService.deleteByUserAndTarget(userId, targetId, type);
    }
    
    /**
     * 查询我的收藏列表（院校或专业）
     * @param userId 用户ID
     * @param type 收藏类型（1:院校, 2:专业）
     * @return 收藏列表
     */
    @Operation(summary = "查询我的收藏列表")
    @GetMapping("/list")
    public List<Favorite> list(@RequestParam Long userId, @RequestParam Integer type) {
        return favoriteService.selectByUserId(userId, type);
    }
    
    /**
     * 判断是否已收藏（院校或专业）
     * @param userId 用户ID
     * @param targetId 目标ID（院校ID或专业ID）
     * @param type 收藏类型（1:院校, 2:专业）
     * @return 是否已收藏
     */
    @Operation(summary = "判断是否已收藏")
    @GetMapping("/check")
    public Boolean check(@RequestParam Long userId, @RequestParam Long targetId, @RequestParam Integer type) {
        return favoriteService.countByUserAndTarget(userId, targetId, type) > 0;
    }
}
