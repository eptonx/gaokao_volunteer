package com.example.gaokaoproject.controller;

import com.example.gaokaoproject.entity.InstitutionUser;
import com.example.gaokaoproject.service.InstitutionUserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/institutionUser")
@Tag(name = "院校用户端")
public class InstitutionUserController {

    @Autowired
    private InstitutionUserService institutionUserService;

    @GetMapping("/selectAll")
    public List<InstitutionUser> selectAll() {
        return institutionUserService.selectAll();
    }

    @GetMapping("/getOne")
    public InstitutionUser getOne(@RequestParam Long id) {
        return institutionUserService.selectById(id);
    }

    @PostMapping("/add")
    public Integer add(@RequestBody InstitutionUser institutionUser) {
        return institutionUserService.add(institutionUser);
    }

    @PutMapping("/update")
    public Integer update(@RequestBody InstitutionUser institutionUser) {
        return institutionUserService.update(institutionUser);
    }

    @DeleteMapping("/delete")
    public Integer delete(@RequestParam Long id) {
        return institutionUserService.delete(id);
    }

    // ========== 新增接口 ==========

    // 注册（带用户名重复检查，返回含ID的用户对象）
    @PostMapping("/register")
    public InstitutionUser register(@RequestBody InstitutionUser user) {
        return institutionUserService.register(user);
    }

    // 登录
    @PostMapping("/login")
    public InstitutionUser login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String ip = body.getOrDefault("ip", "");
        return institutionUserService.login(username, password, ip);
    }

    // 改密码
    @PutMapping("/changePassword")
    public Integer changePassword(@RequestBody Map<String, String> body) {
        Long id = Long.valueOf(body.get("id"));
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        return institutionUserService.changePassword(id, oldPassword, newPassword);
    }

    // 登出
    @PostMapping("/logout")
    public Integer logout(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        return institutionUserService.logout(id);
    }
}