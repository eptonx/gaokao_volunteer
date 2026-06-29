package com.example.gaokaoproject.service.impl;

import com.example.gaokaoproject.entity.Region;
import com.example.gaokaoproject.mapper.RegionMapper;
import com.example.gaokaoproject.service.RegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegionServiceImpl implements RegionService {

    @Autowired
    private RegionMapper regionMapper;

    @Override
    public List<Region> selectRegionAll() {
        return regionMapper.selectAll();
    }

    @Override
    public Region selectRegionById(Long id) {
        return regionMapper.selectById(id);
    }

    @Override
    public Integer updateRegionOne(Region region) {
        return regionMapper.updateOne(region);
    }

    @Override
    public Integer addRegion(Region region) {
        return regionMapper.add(region);
    }

    @Override
    public Integer deleteRegion(Long id) {
        return regionMapper.delete(id);
    }
}