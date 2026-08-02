package org.example.blog.config;

import org.example.blog.interceptor.TokenInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // 这是一个配置类
public class WebConfig implements WebMvcConfigurer {

    //获取
    private final TokenInterceptor tokenInterceptor;

    //上传目录(从配置读取,便于生产环境切换路径)
    @Value("${upload.path}")
    private String uploadPath;

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
                        "/notification/subscribe",
                        "/auth/**",
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
                .allowedOrigins("http://localhost:5173", "http://localhost:5174")
                // 允许所有 HTTP 方法(POST, GET, PUT, DELETE)
                .allowedMethods("*")
                //允许所有请求头
                .allowedHeaders("*")
                //允许携带凭证
                .allowCredentials(true);
    }

    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 目录路径需以分隔符结尾,否则 Spring 资源处理器不把它当目录
        String location = "file:" + uploadPath;
        if (!location.endsWith("/") && !location.endsWith("\\")) {
            location += "/";
        }
        registry.addResourceHandler("/upload/**")
                .addResourceLocations(location);
    }
}
