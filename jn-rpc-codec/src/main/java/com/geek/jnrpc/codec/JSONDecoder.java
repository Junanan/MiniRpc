package com.geek.jnrpc.codec;

import com.alibaba.fastjson.JSON;

/**
 * 实现Decoder接口,基于json的序列化实现
 */
public class JSONDecoder implements Decoder {

    @Override
    public <T> T decode(byte[] bytes, Class<T> clazz) {
        return JSON.parseObject(bytes,clazz);
    }
}
