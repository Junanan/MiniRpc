package com.geek.jnrpc.client;

import com.geek.jnrpc.codec.Decoder;
import com.geek.jnrpc.codec.Encoder;
import com.geek.jnrpc.common.utils.ReflectionUtils;

import java.lang.reflect.Proxy;

public class RpcClient {
    private RpcClientConfig config;
    private Encoder encoder;
    private Decoder decoder;
    private TransportSelector selector;

    public RpcClient(){
        this(new RpcClientConfig());
    }
    public RpcClient(RpcClientConfig config) {
        this.config = config;
        //实例化
        this.encoder = ReflectionUtils.newInstance(this.config.getEncoderClass());
        this.decoder = ReflectionUtils.newInstance(this.config.getDecoderClass());
        this.selector = ReflectionUtils.newInstance(this.config.getSelectorClass());
        //select初始化
        this.selector.init(
                this.config.getServers(),
                this.config.getConnectCount(),
                this.config.getTransportClass()
        );
    }
    // 获取一个接口的代理对象
    public<T> T  getProxy(Class<T> clazz){
        return (T)Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{clazz},
                new RemoteInvoker(clazz,encoder,decoder,selector)
        );
    }
}
