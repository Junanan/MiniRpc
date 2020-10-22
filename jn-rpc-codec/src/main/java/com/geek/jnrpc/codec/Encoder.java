package com.geek.jnrpc.codec;

/**
 * 序列化
 * 把对象转成byte[]数组
 */
public interface Encoder {
    byte[] encode(Object obj);
}
