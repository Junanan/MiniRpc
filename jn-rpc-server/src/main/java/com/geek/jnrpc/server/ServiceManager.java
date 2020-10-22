package com.geek.jnrpc.server;

import com.geek.jnrpc.Request;
import com.geek.jnrpc.ServiceDescriptor;
import com.geek.jnrpc.common.utils.ReflectionUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理rpc暴露的服务
 *
 */
@Slf4j
public class ServiceManager {
    private Map<ServiceDescriptor,ServiceInstance> services;

    public ServiceManager(){
        this.services = new ConcurrentHashMap<>();
    }
    //注册服务
    //作用：将interfaceClass中的所有方法扫描出来，然后与bean绑定起来形成ServiceInstance 放到map中
    //最终将 interfaceClass 中所有方法扫描注册到ServiceManager中
    //interfaceClass 接口 bean 服务提供者 使用泛型将两者联系起来
    public<T> void register(Class<T> interfaceClass,T bean){
        Method[] methods = ReflectionUtils.getPublicMethods(interfaceClass);
        for(Method method : methods){
            ServiceInstance sis = new ServiceInstance(bean,method);
            ServiceDescriptor sdp = ServiceDescriptor.from(interfaceClass,method);

            services.put(sdp,sis);
            //
            log.info("register service : {} {}",sdp.getClazz(),sdp.getMethod());
        }
    }
    //查找
    public ServiceInstance lookup(Request request){
        ServiceDescriptor sdp = request.getService();
        return services.get(sdp);
    }
}
