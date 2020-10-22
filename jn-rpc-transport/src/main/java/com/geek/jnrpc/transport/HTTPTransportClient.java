package com.geek.jnrpc.transport;

import com.geek.jnrpc.Peer;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
public class HTTPTransportClient implements TransportClient {
    private String url;
    @Override

//HTTPClient 是基于短连接的 connect 并不需要去连接server 但是需要url
    public void connect(Peer peer) {

        this.url = "http://" + peer.getHost() + ":" + peer.getPort();
    }

    @Override
    public InputStream write(InputStream data) {
        try {
            HttpURLConnection httpConn = (HttpURLConnection) new URL(url).openConnection();
            //openConnection 返回一个 URLConnection 对象，它表示到 URL 所引用的远程对象的连接。
            httpConn.setDoOutput(true);//允许写出
            httpConn.setDoInput(true);//允许读入
            httpConn.setUseCaches(false);//不使用缓存
            httpConn.setRequestMethod("POST");//设置请求方式为POST

            httpConn.connect();//连接
            //需要把data数据发送给server 使用下面的方法进行实现
            IOUtils.copy(data,httpConn.getOutputStream());

            int resultCode = httpConn.getResponseCode();//获取状态码
            if(resultCode == HttpURLConnection.HTTP_OK){
                //getInputStream()只是得到一个流对象，并不是数据，不过我们可以从流中读出数据
                //getInputStream()得到一个流对象，从这个流对象中只能读取一次数据，第二次读取时将会得到空数据。
                return  httpConn.getInputStream(); //正常则拿到输出
            }else {
                return httpConn.getErrorStream();
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
    //因为是短连接 没有和server连接 所以就不需要关闭连接
    @Override
    public void close() {

    }
}
