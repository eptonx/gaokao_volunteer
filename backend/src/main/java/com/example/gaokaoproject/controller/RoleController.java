package com.example.gaokaoproject.controller;

import com.example.gaokaoproject.entity.Role;
import com.example.gaokaoproject.res.Result;
import com.example.gaokaoproject.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    // 查询全部角色（前端路由 /role/list）
    @GetMapping("/list")
    public Result<List<Role>> list() {
        List<Role> list = roleService.selectAll();
        return Result.ok(list);
    }

    // 根据id查询单个角色
    @GetMapping("/getOne")
    public Result<Role> getOne(@RequestParam Long id) {
        Role role = roleService.selectById(id);
        return Result.ok(role);
    }

    // 新增角色
    @PostMapping("/add")
    public Result<Integer> add(@RequestBody Role role) {
        Integer rows = roleService.add(role);
        return Result.ok(rows);
    }

    // 修改角色信息
    @PutMapping("/update")
    public Result<Integer> update(@RequestBody Role role) {
        Integer rows = roleService.update(role);
        return Result.ok(rows);
    }

    // 启用/禁用角色
    @PutMapping("/updateStatus")
    public Result<Integer> updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        Integer rows = roleService.updateStatus(id, status);
        return Result.ok(rows);
    }

    // 根据id删除角色
    @DeleteMapping("/delete")
    public Result<Integer> deleteOne(@RequestParam Long id) {
        Integer rows = roleService.delete(id);
        return Result.ok(rows);
    }
}