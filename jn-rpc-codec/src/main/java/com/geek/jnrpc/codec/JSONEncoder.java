package com.geek.jnrpc.codec;

import com.alibaba.fastjson.JSON;

/**
 * 实现Encoder接口,基于json的序列化实现
 */
public class JSONEncoder implements Encoder {
    @Override
    public byte[] encode(Object obj) {
        return JSON.toJSONBytes(obj);
    }
}
