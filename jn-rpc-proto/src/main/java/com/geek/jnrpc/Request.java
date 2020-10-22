package com.geek.jnrpc;

import lombok.Data;

/**
 * 表示rpc的一个请求
 */
@Data
public class Request {
    private ServiceDescriptor service;//请求服务类型
    private Object[] parameters;//请求服务参数
}
