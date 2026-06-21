package org.example.blog.config;

import lombok.extern.slf4j.Slf4j;
import org.example.blog.dto.response.Result;
import org.example.blog.exception.BadRequestException;
import org.example.blog.exception.ForbiddenException;
import org.example.blog.exception.NotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleException(Exception e){
        log.error("未捕获异常", e);
        return Result.error(400, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e){
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        log.warn("参数校验失败: {}", message);
        return Result.error(400, message);
    }

    @ExceptionHandler(NotFoundException.class)
    public Result<Void> handleNotFoundException(NotFoundException e){
        log.warn("资源不存在: {}", e.getMessage());
        return Result.error(404, e.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    public Result<Void> handleForbiddenException(ForbiddenException e){
        log.warn("权限不足: {}", e.getMessage());
        return Result.error(403, e.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public Result<Void> handleBadRequestException(BadRequestException e){
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(400, e.getMessage());
    }
}
