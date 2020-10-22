package com.geek.jnrpc.server;

import com.geek.jnrpc.codec.Decoder;
import com.geek.jnrpc.codec.Encoder;
import com.geek.jnrpc.codec.JSONDecoder;
import com.geek.jnrpc.codec.JSONEncoder;
import com.geek.jnrpc.transport.HTTPTransportServer;
import com.geek.jnrpc.transport.TransportServer;
import lombok.Data;

/**
 * server配置
 */
@Data
public class RpcServerConfig {
    private Class<? extends TransportServer> transportClass = HTTPTransportServer.class;
    private Class<? extends Encoder> encoderClass = JSONEncoder.class;
    private Class<? extends Decoder> decoderClass = JSONDecoder.class;
    private int port = 3000;
}
