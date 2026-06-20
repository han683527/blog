package org.example.blog.config;


import org.example.blog.dto.response.Result;
import org.example.blog.exception.BadRequestException;
import org.example.blog.exception.ForbiddenException;
import org.example.blog.exception.NotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice // 该注释实现的是全局拦截 Controller 异常,统一处理
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)// 指定捕获 xxx.class 某种异常
    public Result<Void> handleException(Exception e){
        return Result.error(400,e.getMessage());
    }

    // 参数校验失败
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e){
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        return Result.error(400,message);
    }

    // 资源不存在
    @ExceptionHandler(NotFoundException.class)
    public Result<Void> handleNotFoundException(NotFoundException e){
        return Result.error(404,e.getMessage());
    }

    // 没有权限
    @ExceptionHandler(ForbiddenException.class)
    public Result<Void> handleForbiddenException(ForbiddenException e){
        return Result.error(403,e.getMessage());
    }

    // 参数错误/业务冲突
    @ExceptionHandler(BadRequestException.class)
    public Result<Void> handleBadRequestException(BadRequestException e){
        return Result.error(400,e.getMessage());
    }
}
