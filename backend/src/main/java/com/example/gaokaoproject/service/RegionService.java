package com.example.gaokaoproject.service;

import com.example.gaokaoproject.entity.Region;
import java.util.List;

public interface RegionService {
    List<Region> selectRegionAll();

    Region selectRegionById(Long id);

    Integer updateRegionOne(Region region);

    Integer addRegion(Region region);

    Integer deleteRegion(Long id);
}