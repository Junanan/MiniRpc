package com.geek.jnrpc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;
import java.util.Arrays;

/*
* 表示服务*/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceDescriptor {
    private String clazz;//类名
    private String method;//方法名
    private String returnType;//返回参数
    private String [] parameterTypes;//参数类型
//from 作用 在server 模块中 通过 interfaceClass与method获取到ServiceDescriptor
    public static ServiceDescriptor from(Class clazz, Method method){
        ServiceDescriptor sdp = new ServiceDescriptor();
        sdp.setClazz(clazz.getName());
        sdp.setMethod(method.getName());
        sdp.setReturnType(method.getReturnType().getName());

        Class[] parameterClasses = method.getParameterTypes();
        String[] parameterTypes = new String[parameterClasses.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            parameterTypes[i] = parameterClasses[i].getName();
        }
        sdp.setParameterTypes(parameterTypes);
        return sdp;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    //在ServiceManager 中  services map的key 类型为ServiceDescriptor get方法是需要equal方法判断的 所以要重写equal方法
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o==null|| getClass() != o.getClass()) return false;

        ServiceDescriptor that = (ServiceDescriptor) o;
        return this.toString().equals(that.toString());
    }

    @Override
    public String toString() {
        return "clazz=" +clazz
                + ",method=" + method
                + ",returnType="+returnType
                + ",parameterTypes=" +Arrays.toString(parameterTypes);
    }
}
