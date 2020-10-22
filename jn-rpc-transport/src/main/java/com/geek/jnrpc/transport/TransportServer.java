package com.geek.jnrpc.transport;

/**
 * 1.启动、监听端口
 * 2.接受请求   //接到的请求只是一个String byte 数据流 需要hangdler抽象
 * 3.关闭监听
 */
public interface TransportServer {
    void init(int port,RequestHandler handler); //初始化

    void start();

    void stop();
}
