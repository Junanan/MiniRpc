package com.geek.jnrpc.client;

import com.geek.jnrpc.Peer;
import com.geek.jnrpc.transport.TransportClient;

import java.util.List;

/**
 * 表示选择哪个server去连接
 *
 */
public interface TransportSelector {
    /**
     * 初始化selector
     *
     * @param peers 所有可以连接的server端点信息
     * @param count client与server建立多少个连接
     * @param clazz client实现class
     */
    void init(List<Peer> peers,int count,Class<? extends TransportClient> clazz);
    /**
     * 选择一个transport与server做交互
     *
     */
    TransportClient select();

    /**
     * 释放用完的client
     */
    void release(TransportClient client);

    void close();
}
