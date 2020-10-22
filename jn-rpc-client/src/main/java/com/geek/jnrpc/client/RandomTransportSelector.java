package com.geek.jnrpc.client;

import com.geek.jnrpc.Peer;
import com.geek.jnrpc.common.utils.ReflectionUtils;
import com.geek.jnrpc.transport.TransportClient;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
//实现一个随机的网络端点
public class RandomTransportSelector implements TransportSelector {
    /**
     * 已经连接好的client
     */
    private List<TransportClient> clients;

    public RandomTransportSelector() {
        clients = new ArrayList<>();
    }
    // synchronized 因为client涉及到增删 如果遇到多线程操作可能线程不安全，所以使用synchronize
    @Override
    public synchronized void init(List<Peer> peers, int count, Class<? extends TransportClient> clazz) {
        //ocunt 必须是>=1的
         count = Math.max(count,1);
         //循环建立连接
         for(Peer peer: peers){
             for (int i = 0; i <count ; i++) {
                 //实例化
                 TransportClient client = ReflectionUtils.newInstance(clazz);
                 //连接sever
                 client.connect(peer);
                 //放到list里
                 clients.add(client);
             }
             log.info("connect server : {}",peer);
         }
    }
    //从list从随机取一个链接
    @Override
    public synchronized TransportClient select() {
        int i = new Random().nextInt(clients.size());

        return clients.remove(i);
    }
    //将连接加到list中供下次使用
    @Override
    public synchronized void release(TransportClient client) {

        clients.add(client);
    }
    //把所有的client给关闭掉
    @Override
    public synchronized void close() {
    for (TransportClient client:clients){
        client.close();
    }
    clients.clear();//关闭之后将list清理
    }
}
