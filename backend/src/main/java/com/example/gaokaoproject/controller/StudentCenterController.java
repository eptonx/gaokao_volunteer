package com.example.gaokaoproject.controller;

import com.example.gaokaoproject.entity.User;
import com.example.gaokaoproject.entity.VolunteerPlan;
import com.example.gaokaoproject.res.Result;
import com.example.gaokaoproject.service.UserService;
import com.example.gaokaoproject.service.VolunteerPlanService;
import com.example.gaokaoproject.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/student/center")
@Tag(name = "考生个人中心", description = "考生个人中心相关接口")
public class StudentCenterController {

    @Autowired
    private UserService userService;

    @Autowired
    private VolunteerPlanService volunteerPlanService;

    @Autowired
    private JwtUtil jwtUtil;

    private Long getUserId(String authHeader) {
        String token = authHeader.substring(7);
        return jwtUtil.getUserIdFromToken(token);
    }

    @Operation(summary = "查询个人信息")
    @GetMapping("/info")
    public User getUserInfo(@RequestParam Long userId) {
        return userService.selectById(userId);
    }

    @Operation(summary = "更新个人信息")
    @PutMapping("/update")
    public Result<Integer> updateUserInfo(@RequestBody User user) {
        if (user.getId() == null) {
            return Result.error("用户ID不能为空");
        }
        Integer rows = userService.updateUser(user);
        if (rows == 0) {
            return Result.error("更新失败，用户不存在");
        }
        return Result.ok(rows);
    }

    @Operation(summary = "修改密码")
    @PostMapping("/changePassword")
    public Result<String> changePassword(@RequestBody Map<String, String> body,
                                         @RequestHeader("Authorization") String auth) {
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        if (oldPassword == null || newPassword == null || newPassword.length() < 6) {
            return Result.error("旧密码不能为空，新密码至少6位");
        }
        Long userId = getUserId(auth);
        User user = userService.selectById(userId);
        if (user == null || !oldPassword.equals(user.getPasswordHash())) {
            return Result.error("旧密码不正确");
        }
        userService.updatePassword(userId, newPassword);
        return Result.ok("密码修改成功");
    }

    @Operation(summary = "查询方案锁定记录")
    @GetMapping("/lockRecords")
    public List<VolunteerPlan> getLockRecords(@RequestParam Long userId) {
        return volunteerPlanService.selectLockRecordsByUserId(userId);
    }
}
