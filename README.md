# MiniRpc
## 技术栈
- 1.基础：javacore ，maven ，反射  
- 2.动态代理： java动态代理  
- 3.序列化：java对象与二进制数据互转 fastjson  
- 4.网络通信：用来传输序列化后的数据 jetty，URLConnection 
## 项目模块
- 1 client 客户端  
- 2 server 服务  
- 3 proto 协议  
- 4 codec 编解码  
- 5 transport 数据传输  
- 6 common 工具  
- 7 example 测试样例  
## proto  
>  Proto模块用于规定数据传输协议和规约，其主要类有3个
> > Request 表示rpc的一个请求  
> Response  表示RPC返回  
> ServiceDescriptor  表示服务
## common
> common模块主要为一些反射工具
> > getPublicMethods 获取某个class的共有方法  
> > invoke 调用指定对象的指定方法
## codec
> 序列化模块，将对象与二进制数组相互转换
> > Decoder 反序列化 把byte[]数组转化成对象  
> > Encoder 序列化  把对象转成byte[]数组  
## Transport 
> 该模块主要用于client与server的http通信处理问题，其client请求内容以Request类形式封装传输，server响应内容以Reponse类封装返回。
> > HTTPTransportClient类
```
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
```
> > HTTPTransportServer类

```
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
```
## Server
>   本项目最核心两个模块之一，主要作用是定义了处理client请求的方法
> > 部分Rpcserver类代码

```
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
```
> > ServiceManager  管理Rpc暴露的服务

```
@Slf4j
public class ServiceManager {
    private Map<ServiceDescriptor,ServiceInstance> services;

    public ServiceManager(){
        this.services = new ConcurrentHashMap<>();
    }
    //注册服务
    //将interfaceClass中的所有方法扫描出来，然后与bean绑定起来形成ServiceInstance 放到map中
    //最终将 interfaceClass 中所有方法扫描注册到ServiceManager中
    //interfaceClass 接口 bean 服务提供者 使用泛型将两者联系起来
    public<T> void register(Class<T> interfaceClass,T bean){
        Method[] methods = ReflectionUtils.getPublicMethods(interfaceClass);
        for(Method method : methods){
            ServiceInstance sis = new ServiceInstance(bean,method);
            ServiceDescriptor sdp = ServiceDescriptor.from(interfaceClass,method);

            services.put(sdp,sis);
            //
            log.info("register service : {} {}",sdp.getClazz(),sdp.getMethod());
        }
    }
    //查找
    public ServiceInstance lookup(Request request){
        ServiceDescriptor sdp = request.getService();
        return services.get(sdp);
    }
```
## Client
> 该模块主要功能有连个一个时动态代理获取实参，一个是请求Server进行过程调用。
> > 其RpcClient类实现如下

```
public class RpcClient {
    private RpcClientConfig config;
    private Encoder encoder;
    private Decoder decoder;
    private TransportSelector selector;

    public RpcClient(){
        this(new RpcClientConfig());
    }
    public RpcClient(RpcClientConfig config) {
        this.config = config;
        //实例化
        this.encoder = ReflectionUtils.newInstance(this.config.getEncoderClass());
        this.decoder = ReflectionUtils.newInstance(this.config.getDecoderClass());
        this.selector = ReflectionUtils.newInstance(this.config.getSelectorClass());
        //select初始化
        this.selector.init(
                this.config.getServers(),
                this.config.getConnectCount(),
                this.config.getTransportClass()
        );
    }
    // 获取一个接口的代理对象
    public<T> T  getProxy(Class<T> clazz){
        return (T)Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{clazz},
                new RemoteInvoker(clazz,encoder,decoder,selector)
        );
    }
```
> > 该类需要注意两点，之一是TransportSelector对象，其实现如下：

```
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
```
> > 该类主要是用于处理Client对Server的连接问题，相当于连接池，由有需求时随机返回连接，再重点关注RemoteInvoker类，invoke()方法中对代理方法的参数进行存储封装到Request对象并且最终序列化传递到Server  
> > RemoteInvoker部分代码

```
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
```
## example
> 案例模块，简单的使用rpc框架实现分布式计算器
> > 先启动server

```
public static void main(String[] args) {
        RpcServer server = new RpcServer(new RpcServerConfig()); //使用默认配置
        server.register(CalcService.class,new CalcServiceImpl()); //把服务注册进去
        server.start();
    }
```
> > 然后在运行client

```
public static void main(String[] args) {
        RpcClient client = new RpcClient(new RpcClientConfig());//使用默认配置
        CalcService service = client.getProxy(CalcService.class); //拿到远程的代理对象

        int r1 = service.add(1,2);
        int r2 = service.minus(3,2);

        System.out.println(r1);
        System.out.println(r2);
    }
```
