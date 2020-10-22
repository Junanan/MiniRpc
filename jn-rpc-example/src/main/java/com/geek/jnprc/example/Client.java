package com.geek.jnprc.example;

import com.geek.jnrpc.client.RpcClient;
import com.geek.jnrpc.client.RpcClientConfig;

public class Client {
    public static void main(String[] args) {
        RpcClient client = new RpcClient(new RpcClientConfig());//使用默认配置
        CalcService service = client.getProxy(CalcService.class); //拿到远程的代理对象

        int r1 = service.add(1,2);
        int r2 = service.minus(3,2);

        System.out.println(r1);
        System.out.println(r2);
    }
}
