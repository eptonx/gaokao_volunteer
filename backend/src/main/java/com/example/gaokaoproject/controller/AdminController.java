package com.example.gaokaoproject.controller;

import com.example.gaokaoproject.res.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import com.example.gaokaoproject.entity.Admin;
import com.example.gaokaoproject.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

// 完全对齐demo里StudentController注解、请求方式、传参写法
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // 分页查询管理员列表（支持关键词搜索）
    @GetMapping("/selectAll")
    public Result<List<Admin>> selectAll(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword
    ) {
        List<Admin> list = adminService.selectByPage(pageNum, pageSize, keyword);
        return Result.ok(list);
    }

    // 根据id查询单个管理员（id是Long类型匹配数据库BIGINT）
    @GetMapping("/getOne")
    public Result<Admin> getOne(@RequestParam Long id) {
        Admin admin = adminService.selectById(id);
        return Result.ok(admin);
    }

    // 新增管理员，json请求体接收对象
    @PostMapping("/add")
    public Result<Integer> add(@RequestBody Admin admin) {
        Integer rows = adminService.add(admin);
        return Result.ok(rows);
    }
    // 修改管理员信息（不传密码则保留原密码）
    @PutMapping("/update")
    public Result<Integer> update(@RequestBody Admin admin) {
        // 编辑时如果没传passwordHash，保留数据库原密码
        if (admin.getPasswordHash() == null || admin.getPasswordHash().isEmpty()) {
            Admin existing = adminService.selectById(admin.getId());
            if (existing != null) {
                admin.setPasswordHash(existing.getPasswordHash());
            }
        }
        Integer rows = adminService.update(admin);
        return Result.ok(rows);
    }

    // 根据id删除管理员
    @DeleteMapping("/delete")
    public Result<Integer> deleteOne(@RequestParam Long id) {
        Integer rows = adminService.delete(id);
        return Result.ok(rows);
    }

    // 启用/禁用管理员
    @PutMapping("/updateStatus")
    public Result<Integer> updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        Integer rows = adminService.updateStatus(id, status);
        return Result.ok(rows);
    }

    // 分配角色
    @PutMapping("/updateRoleId")
    public Result<Integer> updateRoleId(@RequestParam Long id, @RequestParam Long roleId) {
        Integer rows = adminService.updateRoleId(id, roleId);
        return Result.ok(rows);
    }

    // 正式登录接口
    @PostMapping("/login")
    public Result<Admin> login(
            @RequestBody Map<String, String> body,
            HttpServletRequest request
    ) {
        String username = body.get("username");
        String password = body.get("password");
        // 获取客户端IP
        String clientIp = request.getRemoteAddr();
        // 调用完整登录业务方法
        Admin loginAdmin = adminService.login(username, password, clientIp);
        // 登录信息存入Session，后端保存登录状态
        HttpSession session = request.getSession();
        session.setAttribute("loginAdmin", loginAdmin);
        // 统一Result包装返回
        return Result.ok(loginAdmin);
    }

    // 后端登出接口
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        HttpSession session = request.getSession();
        // 销毁会话，清空登录状态
        session.invalidate();
        return Result.ok("登出成功");
    }

    // 修改个人密码
    @PostMapping("/updatePwd")
    public Result<Integer> updatePwd(
            @RequestBody Map<String, String> body,
            HttpServletRequest request
    ) {
        String oldPwd = body.get("oldPwd");
        String newPwd = body.get("newPwd");
        // 取出当前登录账号
        HttpSession session = request.getSession();
        Admin loginAdmin = (Admin) session.getAttribute("loginAdmin");
        // 调用业务
        Integer rows = adminService.updatePwd(loginAdmin.getId(), oldPwd, newPwd);
        // 修改密码后销毁session，强制重新登录
        session.invalidate();
        return Result.ok(rows);
    }
}
