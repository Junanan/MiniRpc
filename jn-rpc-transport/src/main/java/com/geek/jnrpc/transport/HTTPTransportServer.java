package com.geek.jnrpc.transport;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
@Slf4j
//HTTPSever基于jetty
public class HTTPTransportServer implements TransportServer {
    private RequestHandler handler;
    private Server server;
    @Override
    public void init(int port, RequestHandler handler) {
        this.handler= handler;
        this.server = new Server(port);
        //接受请求基于servlet
        ServletContextHandler ctx = new ServletContextHandler();
        server.setHandler(ctx);
        //holder 是jetty是处理网络请求的一个抽象
        ServletHolder holder = new ServletHolder(new RequestServlet());
        ctx.addServlet(holder,"/*"); //处理所有的路径
    }

    @Override
    public void start() {
        try {
            server.start();
            server.join();
        } catch (Exception e) {
            log.error(e.getMessage(),e);//用@Slf4j 自动生成log对象
        }
    }

    @Override
    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
    }
    //servlet doPost重写
    class RequestServlet extends HttpServlet{
        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            log.info("client connect");

            InputStream in = req.getInputStream();
            OutputStream out = resp.getOutputStream();

            if(handler !=null){
                handler.onRequest(in,out);
            }
            out.flush();
        }
    }
}
