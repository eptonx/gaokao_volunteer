package com.example.gaokaoproject.controller;

import com.example.gaokaoproject.entity.Region;
import com.example.gaokaoproject.service.RegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/region")
public class RegionController {

    @Autowired
    private RegionService regionService;

    @GetMapping("/selectAll")
    public List<Region> selectAll() {
        return regionService.selectRegionAll();
    }

    @GetMapping("/getOne")
    public Region getOne(@RequestParam Long id) {
        return regionService.selectRegionById(id);
    }

    @PostMapping("/add")
    public Integer add(@RequestBody Region region) {
        return regionService.addRegion(region);
    }

    @PutMapping("/update")
    public Integer update(@RequestBody Region region) {
        return regionService.updateRegionOne(region);
    }

    @DeleteMapping("/delete")
    public Integer delete(@RequestParam Long id) {
        return regionService.deleteRegion(id);
    }
}