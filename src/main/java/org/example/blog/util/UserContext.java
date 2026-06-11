package org.example.blog.util;

//使用 ThreadLocal 全局变量的请求(上下文) 替代 HttpServletRequest 传递 userId
public class UserContext {
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    // 存 userId
    public static void set(Long userId){
        USER_ID.set(userId);
    }

    // 取 userId
    public static Long get(){
        return USER_ID.get();
    }

    // 清除
    public static void remove(){
        USER_ID.remove();
    }
}
