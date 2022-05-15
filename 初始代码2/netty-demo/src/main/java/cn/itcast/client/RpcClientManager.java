package cn.itcast.client;

import cn.itcast.client.handler.RpcResponseMessageHandler;
import cn.itcast.message.RpcRequestMessage;
import cn.itcast.protocol.MessageCodecSharable;
import cn.itcast.protocol.ProcotolFrameDecoder;
import cn.itcast.protocol.SequenceIdGenerator;
import cn.itcast.server.service.HelloService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;

@Slf4j
public class RpcClientManager {


    public static void main(String[] args) {
        /**
         *      主线程发起的调用，RpcResponseMessageHandler拿到的响应消息，即接收结果的是 Netty 的 NIO eventLoop线程
         *      说明这是两个线程间的通信拿结果的问题，可以使用 promise 来进行通信：
         *          主线程把一个空的书包给你，NIO线程什么时候拿到响应了把结果放到这个空书包里，主线程再从书包里拿结果
         * */
        HelloService service = getProxyService(HelloService.class);
        System.out.println(service.sayHello("zhangsan"));
//        System.out.println(service.sayHello("lisi"));
//        System.out.println(service.sayHello("wangwu"));
    }

    // 创建代理类，传入需要拦截的接口（实现类）
    public static <T> T getProxyService(Class<T> serviceClass) {
        ClassLoader loader = serviceClass.getClassLoader();
        Class<?>[] interfaces = new Class[]{serviceClass};
        // sayHello  "张三"

     /**
    生成得到的代理类
    public Object getProxy(){
        return Proxy.newProxyInstance(this.getClass().getClassLoader(), rent.getClass().getInterfaces(), this );
    }
    处理代理实例并返回结果
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {}
    */

        // Proxy.newProxyInstance(类加载器, 代理类要实现的接口, (代理类对象, 方法, 参数)）
        Object o = Proxy.newProxyInstance(loader, interfaces, (proxy, method, args) -> {
            // 1. 将方法调用转换为 消息对象（转换为变量，不固定）
            int sequenceId = SequenceIdGenerator.nextId();
            RpcRequestMessage msg = new RpcRequestMessage(
                    sequenceId,                     //使用AtomicInteger自增产生
                    serviceClass.getName(),         //接口全限名
                    method.getName(),               //方法名
                    method.getReturnType(),         //方法返回值类型
                    method.getParameterTypes(),     //方法参数类型
                    args                            //参数
            );
            // 2. 将消息对象发送出去
            getChannel().writeAndFlush(msg);

            /**
             * jdk Future 只能同步等待任务结束（或成功、或失败）才能得到结果
             * netty Future 可以同步等待任务结束得到结果，也可以异步方式得到结果(addListener)，但都是要等任务结束
             * netty Promise 不仅有 netty Future 的功能，而且脱离了任务独立存在，只作为两个线程间传递结果的容器
             * */
            // 3. 准备一个空 Promise对象（空书包），来接收结果             指定 promise 对象异步接收结果线程
            DefaultPromise<Object> promise = new DefaultPromise<>(getChannel().eventLoop());
            // 并把这个对象放在 PROMISES 集合中
            RpcResponseMessageHandler.PROMISES.put(sequenceId, promise);

            /**
             *      await	-	等待任务结束，如果任务失败，不会抛异常，而是通过 isSuccess 判断
             *      sync	-	等待任务结束，如果任务失败，抛出异常
             *      getNow	-	获取任务结果，非阻塞，还未产生结果时返回 null
             *      cause	-	获取失败信息，非阻塞，如果没有失败，返回null
             * */
            // 4. 等待 promise 结果
            promise.await();
            if(promise.isSuccess()) {
                // 调用正常返回书包里的结果
                return promise.getNow();
            } else {
                // 调用失败
                throw new RuntimeException(promise.cause());
            }
        });
        return (T) o;
    }

    // 获取唯一的 channel 对象
    private static Channel channel = null;
    private static final Object LOCK = new Object();

    public static Channel getChannel() {
        if (channel != null) {
            return channel;
        }
        synchronized (LOCK) { //  t2
            if (channel != null) { // t1
                return channel;
            }
            initChannel();
            return channel;
        }
    }

    // 初始化 channel 方法
    private static void initChannel() {
        NioEventLoopGroup group = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();
        RpcResponseMessageHandler RPC_HANDLER = new RpcResponseMessageHandler();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.group(group);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new ProcotolFrameDecoder());
                ch.pipeline().addLast(LOGGING_HANDLER);
                ch.pipeline().addLast(MESSAGE_CODEC);
                ch.pipeline().addLast(RPC_HANDLER);
            }
        });
        try {
            channel = bootstrap.connect("localhost", 8080).sync().channel();
            //同步阻塞：然后调用sync()方法阻塞执行操作的线程，等待channel真正关闭后，再执行其他操作
            //异步阻塞：channel 关闭后会触发 关闭 group，但是不阻塞，因为其他线程也要用
            channel.closeFuture().addListener(future -> {
                group.shutdownGracefully();
            });
        } catch (Exception e) {
            log.error("client error", e);
        }
    }
}
