package com.geek.jnrpc.server;

import com.geek.jnrpc.Request;
import com.geek.jnrpc.Response;
import com.geek.jnrpc.codec.Decoder;
import com.geek.jnrpc.codec.Encoder;
import com.geek.jnrpc.common.utils.ReflectionUtils;
import com.geek.jnrpc.transport.RequestHandler;
import com.geek.jnrpc.transport.TransportServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
@Slf4j
public class RpcServer {
    private RpcServerConfig config;
    private TransportServer net;
    private Encoder encoder;
    private Decoder decoder;
    private ServiceManager serviceManager;
    private Serviceinvoker serviceinvoker;
    public RpcServer() {

    }
    //config是外边传进来的
    public RpcServer(RpcServerConfig config){
        this.config = config;
        //使用反射工具将他们的newInstance实例化
        //net
        this.net  = ReflectionUtils.newInstance(config.getTransportClass());
        this.net.init(config.getPort(),this.handler);
        //codec
        this.encoder = ReflectionUtils.newInstance(config.getEncoderClass());
        this.decoder = ReflectionUtils.newInstance(config.getDecoderClass());

        //service
        this.serviceManager = new ServiceManager();
        this.serviceinvoker = new Serviceinvoker();

    }
    //注册方法 serviceManager
    public<T> void register(Class<T> interfaceClass,T bean){
        serviceManager.register(interfaceClass,bean);
    }
    //将网络模块启动起来实现监听
    public void start(){
        this.net.start();
    }
    public void stop(){
        this.net.stop();
    }
    // request请求序列化之后->recive 然后通过Serviceinvoker调用服务，将数据写回toResp 返回到client
    private RequestHandler handler = new RequestHandler() {
        @Override
        public void onRequest(InputStream recive, OutputStream toResp) {
            Response resp = new Response();
            try {
                byte[] inBytes = IOUtils.readFully(recive, recive.available());
                Request request = decoder.decode(inBytes,Request.class);//这里是从client传过来的
                log.info("get request: {}",request);
                //通过request 查找服务
                ServiceInstance sis =serviceManager.lookup(request);
               // 有了服务就可以开始具体调用了
                Object ret = serviceinvoker.invoke(sis,request);
                resp.setData(ret);//执行返回结果
            } catch (Exception e) {
                log.warn(e.getMessage(),e);
                resp.setCode(1);//1为错误
                resp.setMessage("RpcServer got error :"
                +e.getClass().getName()
                +" : " + e.getMessage());
            }finally {
                try {
                    byte[] outBytes = encoder.encode(resp);
                    toResp.write(outBytes);//结果返回给client
                    log.info("response client");
                }catch (IOException e) {
                    log.warn(e.getMessage(), e);
                }
            }
        }
    };
}
