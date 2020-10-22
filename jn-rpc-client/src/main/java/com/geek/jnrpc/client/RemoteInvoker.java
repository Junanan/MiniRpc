package com.geek.jnrpc.client;

import com.geek.jnrpc.Request;
import com.geek.jnrpc.Response;
import com.geek.jnrpc.ServiceDescriptor;
import com.geek.jnrpc.codec.Decoder;
import com.geek.jnrpc.codec.Encoder;
import com.geek.jnrpc.transport.TransportClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 调用远程服务的代理类
 */
@Slf4j
public class RemoteInvoker implements InvocationHandler {
    private Class clazz;
    private Encoder encoder;
    private Decoder decoder;
    private TransportSelector selector;
    public  RemoteInvoker(Class clazz, Encoder encoder, Decoder decoder, TransportSelector selector) {
        this.clazz = clazz;
        this.decoder = decoder;
        this.encoder =encoder;
        this.selector = selector;
    }
    //调用远程服务 首先要构造一个请求 通过网络将这个请求对象发送给server 等待响应 再从响应拿到返回数据
    @Override
    //method 调用的方法，args 调用方法传进的参数
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Request request = new Request();
        request.setService(ServiceDescriptor.from(clazz,method)); //设置请求服务类型
        request.setParameters(args);//设置请求服务参数
        Response resp = invokeRemote(request);
        if(resp == null||resp.getCode()!=0){  //失败
            throw new IllegalStateException("fail to invoke remote: " + resp);
        }

        return resp.getData();
    }

    private Response invokeRemote(Request request) {
        TransportClient client =null;
        Response resp = null;
        try{
            client = selector.select(); //通过selector选择client出来
            byte[] outBytes = encoder.encode(request);
            InputStream revice = client.write(new ByteArrayInputStream(outBytes));
//            readFully
//            这个方法会读取指定长度的流，如果读取的长度不够，就会抛出异常
//            available():返回与之关联的文件的字节数
            byte[] inBytes = IOUtils.readFully(revice,revice.available()); //将revice数据全部读取出来
            resp = decoder.decode(inBytes,Response.class);
        } catch (IOException e) {
            log.warn(e.getMessage(),e);
            resp = new Response();
            resp.setCode(1);
            resp.setMessage("RpcClient got error :"+ e.getClass()+ ":"+e.getMessage());
        } finally {
            if (client!=null){
                selector.release(client);
            }
        }
        return resp;
    }
}
