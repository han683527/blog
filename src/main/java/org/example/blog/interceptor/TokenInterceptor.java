package org.example.blog.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.blog.util.JwtUtil;
import org.example.blog.util.UserContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


@Component//作用是什么?
public class TokenInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //从请求头获取 token
        String token = request.getHeader("Authorization");  //Authorization 为什么是获取这个

        if(token == null || !token.startsWith("Bearer ")){  //token 为空;或不是以 "Bearer" 为前缀
            response.setStatus(401);
            response.getWriter().write("未登录");
            return false;
        }

        token = token.substring(7); //去掉 "Bearer" 前缀拿到真正的 token

        try{
            Long userId = JwtUtil.getUserId(token);
            UserContext.set(userId); // 存入上下文
            request.setAttribute("userId",userId);
            return true;
        } catch (Exception e){
            response.setStatus(401);
            response.getWriter().write("token无效或以过期");
            return false;
        }
    }
}
