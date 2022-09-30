package com.itheima.reggie.common;

/**
 * 基于ThreadLocal封装的一个工具类，用于保存和获取当前登录用户的ID
 * 具体的原理可以见两个说明
 * 由于是工具类，所以都使用static。
 * static方法可以直接用类调用，static变量则不需更改
 */
public class BaseContext {
    private static ThreadLocal<Long> threadLocal=new ThreadLocal<>();

    public static void SetCurrentId(Long id){

        threadLocal.set(id);
    }
    public static Long GetCurrentId(){
        return threadLocal.get();
    }
}
