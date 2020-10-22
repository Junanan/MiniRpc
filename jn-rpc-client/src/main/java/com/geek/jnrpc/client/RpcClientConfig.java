package com.geek.jnrpc.client;

import com.geek.jnrpc.Peer;
import com.geek.jnrpc.codec.Decoder;
import com.geek.jnrpc.codec.Encoder;
import com.geek.jnrpc.codec.JSONDecoder;
import com.geek.jnrpc.codec.JSONEncoder;
import com.geek.jnrpc.transport.HTTPTransportClient;
import com.geek.jnrpc.transport.TransportClient;
import lombok.Data;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
//RpcClient配置
@Data
public class RpcClientConfig {
    private Class<? extends TransportClient> transportClass = HTTPTransportClient.class;
    private Class<? extends Encoder> encoderClass = JSONEncoder.class;
    private Class<? extends Decoder> decoderClass = JSONDecoder.class;
    //路由选择
    private Class<? extends TransportSelector> selectorClass = RandomTransportSelector.class;
    //每个server peer默认建立一个连接
    private int connectCount = 1;
    //可以连哪些链接
    private List<Peer> servers = Arrays.asList(
            new Peer("127.0.0.1",3000) //默认9
    );
}
