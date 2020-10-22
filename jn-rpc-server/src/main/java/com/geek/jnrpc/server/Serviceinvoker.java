package com.geek.jnrpc.server;

import com.geek.jnrpc.Request;
import com.geek.jnrpc.common.utils.ReflectionUtils;

/**
 *
 * 调用具体服务
 */
public class Serviceinvoker {
    public Object invoke(ServiceInstance service, Request request){
        return ReflectionUtils.invoke(
                service.getTarget(),
                service.getMethod(),
                request.getParameters()
        );
    }
}
