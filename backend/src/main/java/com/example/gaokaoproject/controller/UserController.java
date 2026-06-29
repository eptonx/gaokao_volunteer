package com.example.gaokaoproject.controller;

import com.example.gaokaoproject.annotation.LogLogin;
import com.example.gaokaoproject.entity.User;
import com.example.gaokaoproject.res.Result;
import com.example.gaokaoproject.service.UserService;
import com.example.gaokaoproject.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/student")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    private Long getUserId(String authHeader) {
        String token = authHeader.substring(7);
        return jwtUtil.getUserIdFromToken(token);
    }

    @GetMapping("/getByPhone")
    public Result<User> getByPhone(@RequestParam String phone) {
        User user = userService.selectByPhone(phone);
        return Result.ok(user);
    }

    // 根据ID查询用户
    @GetMapping("/getUser")
    public Result<User> getUser(@RequestHeader("Authorization") String auth) {
        User user = userService.selectById(getUserId(auth));
        if (user != null) {
            user.setPasswordHash(null);
        }
        return Result.ok(user);
    }

    // 更新用户信息
    @PutMapping("/update")
    public Result<Integer> updateUser(@RequestBody User user,
                                       @RequestHeader("Authorization") String auth) {
        user.setId(getUserId(auth));
        Integer rows = userService.updateUser(user);
        return Result.ok(rows);
    }

    // 删除用户
    @DeleteMapping("/delete")
    public Result<Integer> deleteUser(@RequestHeader("Authorization") String auth) {
        Integer rows = userService.deleteUser(getUserId(auth));
        return Result.ok(rows);
    }
}