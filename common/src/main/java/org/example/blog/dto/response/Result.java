package org.example.blog.dto.response;

import lombok.Data;

@Data
public class Result<T>{

    private int code;

    private String message;

    private T data;

    // 成功状态
    public  static <T> Result<T> success(T data){
        Result<T> r = new Result<>();
        r.code = 200;
        r.message = "success";
        r.data = data;
        return r;
    }

    // 失败状态
    public static <T> Result<T> error(String message){
        Result<T> r = new Result<>();
        r.code = 400;
        r.message = message;
        r.data = null;
        return r;
    }

    public static <T> Result<T> error(int code,String message){
        Result<T> r = new Result<>();
        r.code = code;
        r.message = message;
        r.data = null;
        return r;
    }
}
