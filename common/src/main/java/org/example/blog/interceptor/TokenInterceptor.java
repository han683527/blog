package org.example.blog.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.blog.util.JwtUtil;
import org.example.blog.util.UserContext;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;


@Component
@RequiredArgsConstructor
public class TokenInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final List<String> optionalPaths = List.of("/article/{id}");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //从请求头获取 token
        String token = request.getHeader("Authorization");  //Authorization 为什么是获取这个
        String path = request.getRequestURI();

        boolean isOptional = optionalPaths.stream().anyMatch(p -> new AntPathMatcher().match(p, path));

        if(token == null || !token.startsWith("Bearer ")){  //token 为空;或不是以 "Bearer" 为前缀
            if(isOptional) return true;
            response.setStatus(401);
            response.getWriter().write("未登录");
            return false;
        }

        token = token.substring(7); //去掉 "Bearer" 前缀拿到真正的 token

        try{
            Long userId = jwtUtil.validateAndGetUserId(token);
            UserContext.set(userId); // 存入上下文
            request.setAttribute("userId",userId);
            return true;
        } catch (Exception e){
            if(isOptional) return true;
            response.setStatus(401);
            response.getWriter().write("token无效或以过期");
            return false;
        }
    }
}
