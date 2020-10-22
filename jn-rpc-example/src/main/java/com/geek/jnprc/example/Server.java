package com.geek.jnprc.example;

import com.geek.jnrpc.server.RpcServer;
import com.geek.jnrpc.server.RpcServerConfig;

public class Server {
    public static void main(String[] args) {
        RpcServer server = new RpcServer(new RpcServerConfig()); //使用默认配置
        server.register(CalcService.class,new CalcServiceImpl()); //把服务注册进去
        server.start();
    }
}
