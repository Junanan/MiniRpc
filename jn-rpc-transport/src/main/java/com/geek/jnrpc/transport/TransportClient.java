package com.geek.jnrpc.transport;

import com.geek.jnrpc.Peer;

import java.io.InputStream;

/**
 * 1.创建连接
 * 2.等待连接，并且等待响应
 * 3.关闭连接
 */
public interface TransportClient {
    void connect(Peer peer);//连接网络端点
    InputStream write(InputStream data);//写完数据后需要等待响应
    void close();
}
