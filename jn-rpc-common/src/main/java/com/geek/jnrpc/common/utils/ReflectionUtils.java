package com.geek.jnrpc.common.utils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * 反射工具类
 */
public class ReflectionUtils {
    /**
     * 根据class创建对象
     * @param clazz 待创建对象的类
     * @param <T> 对象类型
     * @return 创建好的对象
     */
    //<T> T 使用泛型 然后返回泛型 这样更具有一般性 ,如果不使用 则返回object类
    public static <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     *
     * 获取某个class的共有方法
     * @param clazz
     * @return 当前类声明的共有方法
     */
    public static Method[] getPublicMethods(Class clazz){
        Method[] methods = clazz.getDeclaredMethods();//getDeclaredMethods 获取所有的方法
        List<Method> pmethods = new ArrayList<Method>();
        for (Method m : methods) { // 过滤不是public的方法
            if (Modifier.isPublic(m.getModifiers())){
                pmethods.add(m);
            }
        }
        return pmethods.toArray(new Method[0]);//入参数组长度小于真实长度，则自动扩容，所以可以使用Method[0]
    }

    /**
     * 调用指定对象的指定方法
     *
     * @param obj 被调用方法的对象
     * @param method 被调用的方法
     * @param args 方法的参数
     * @return 返回的结果
     */
    public  static Object invoke(Object obj, Method method,Object... args) {
        try {
            return method.invoke(obj,args);
        } catch (Exception e) {
            throw  new IllegalStateException(e);
        }
    }
}
