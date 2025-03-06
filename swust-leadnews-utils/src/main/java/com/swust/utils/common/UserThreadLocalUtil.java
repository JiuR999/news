package com.swust.utils.common;


public class UserThreadLocalUtil {
    private final static ThreadLocal<Long> USER_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 添加用户
     * @param userId
     */
    public static void  setUser(Long userId){
        USER_THREAD_LOCAL.set(userId);
    }

    /**
     * 获取用户
     */
    public static Long getUser(){
        return USER_THREAD_LOCAL.get();
    }

    /**
     * 清理用户
     */
    public static void clear(){
        USER_THREAD_LOCAL.remove();
    }
}
