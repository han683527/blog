package org.example.blog.config;

import org.example.blog.interceptor.TokenInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // 这是一个配置类
public class WebConfig implements WebMvcConfigurer {

    //获取
    private final TokenInterceptor tokenInterceptor;

    //注入
    public WebConfig(TokenInterceptor tokenInterceptor) {
        this.tokenInterceptor = tokenInterceptor;
    }

    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor)
                //拦截所有请求
                .addPathPatterns("/**")
                //排除特定请求
                .excludePathPatterns(
                        "/user/public/{id}",
                        "/auth/**",
                        "/article/list",
                        "/article/{id}",
                        "/comment/list",
                        "/tag/**",
                        "/category/**",
                        "/upload/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**");
    }

    // CORS 跨域处理
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 对所有接口生效
        registry.addMapping("/**")
                // 允许那些前端地址访问
                .allowedOrigins("http://localhost:5173", "http://localhost:6173")
                // 允许所有 HTTP 方法(POST, GET, PUT, DELETE)
                .allowedMethods("*")
                //允许所有请求头
                .allowedHeaders("*")
                //允许携带凭证
                .allowCredentials(true);
    }

    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:D:\\java\\blog\\uploads\\");
    }
}
