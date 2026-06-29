package org.example.blog.config;

import org.example.blog.interceptor.TokenInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // 这是一个配置类
public class WebConfig implements WebMvcConfigurer {

    //获取
    private final TokenInterceptor tokenInterceptor;

    //注入
    public WebConfig(TokenInterceptor tokenInterceptor){
        this.tokenInterceptor = tokenInterceptor;
    }

    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns("/**")                 //拦截所有请求
                .excludePathPatterns("/auth/register",  //放行特定请求
                        "/auth/login",
                        "/auth/code",
                        "/auth/refresh",
                        "/upload/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**");
    }

    public void addResourceHandlers(ResourceHandlerRegistry registry){
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:D:\\java\\blog\\uploads\\");
    }
}
