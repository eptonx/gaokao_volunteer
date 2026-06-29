package com.example.gaokaoproject.config;//当前类所在包，config 放配置 / 拦截器类。

import com.example.gaokaoproject.entity.Admin;//当前类所在包，config 放配置 / 拦截器类。
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component//Spring 注解：把这个拦截器交给 Spring 容器管理，项目启动自动创建对象。
public class LoginInterceptor implements HandlerInterceptor {//自定义登录拦截器

    @Override//重写接口的前置拦截方法preHandle
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {//请求对象，响应对象，当前要执行的控制器方法
        // 放行 OPTIONS 预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;//放行本次请求，继续执行后面的 Controller
        }

        String path = request.getRequestURI();//拿到当前访问的接口路径
        // 放行登录、登出等不需要鉴权的接口
        if (path.contains("/admin/login") || path.contains("/admin/logout")) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(401);//返回401拦截
            response.getWriter().write("{\"code\":401,\"msg\":\"未登录或会话已过期\"}");
            return false;//拦截请求
        }

        Admin loginAdmin = (Admin) session.getAttribute("loginAdmin");//Session 存在，但里面没有登录管理员对象
        if (loginAdmin == null) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"msg\":\"未登录或会话已过期\"}");
            return false;
        }

        return true;
    }
}
