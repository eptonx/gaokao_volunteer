package com.example.gaokaoproject.service.impl;

import com.example.gaokaoproject.entity.Favorite;
import com.example.gaokaoproject.mapper.FavoriteMapper;
import com.example.gaokaoproject.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Override
    public Integer add(Favorite favorite) {
        if (favorite.getCreatedAt() == null) {
            favorite.setCreatedAt(LocalDateTime.now());
        }
        return favoriteMapper.add(favorite);
    }

    @Override
    public Integer update(Favorite favorite) {
        return favoriteMapper.update(favorite);
    }

    @Override
    public Integer delete(Long id) {
        return favoriteMapper.delete(id);
    }

    @Override
    public Integer deleteByUserAndTarget(Long userId, Long targetId, Integer type) {
        return favoriteMapper.deleteByUserAndTarget(userId, targetId, type);
    }

    @Override
    public List<Favorite> selectAll() {
        return favoriteMapper.selectAll();
    }

    @Override
    public Favorite selectById(Long id) {
        return favoriteMapper.selectById(id);
    }

    @Override
    public List<Favorite> selectByUserId(Long userId, Integer type) {
        return favoriteMapper.selectByUserId(userId, type);
    }

    @Override
    public List<Favorite> selectByUserId(Long userId) {
        return favoriteMapper.selectAllByUserId(userId);
    }

    @Override
    public Integer countByUserAndTarget(Long userId, Long targetId, Integer type) {
        return favoriteMapper.countByUserAndTarget(userId, targetId, type);
    }
}
