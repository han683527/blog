package org.example.blog.config;


import org.example.blog.dto.response.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice // 该注释实现的是全局拦截 Controller 异常,统一处理
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)// 指定捕获 xxx.class 某种异常
    public Result<Void> handleException(Exception e){
        return Result.error(e.getMessage());
    }
}
