package cn.itcast.server;

import cn.itcast.protocol.MessageCodecSharable;
import cn.itcast.protocol.ProcotolFrameDecoder;
import cn.itcast.server.handler.RpcRequestMessageHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcServer {
    public static void main(String[] args) {
        //boss 负责处理连接，worker（child）负责处理读写
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();
        RpcRequestMessageHandler RPC_HANDLER = new RpcRequestMessageHandler();
        try {
            //创建服务端启动引导
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            //给引导类配置两⼤线程组,确定了线程模型
            serverBootstrap.group(boss, worker);
            //指定 IO 模型
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                /**
                 * 3、服务器拿到消息后做入栈处理
                 *
                 * 5、响应消息经 出栈处理后 发送给客户端
                 * */
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ProcotolFrameDecoder());  //入 栈处理器：粘包拆包处理
                    //⾃定义客户端消息的业务处理逻辑
                    ch.pipeline().addLast(LOGGING_HANDLER);             //入 出 栈处理器：日志
                    ch.pipeline().addLast(MESSAGE_CODEC);               //入 出 栈处理器：（自定义消息）解码
                    ch.pipeline().addLast(RPC_HANDLER);                 //入 栈最后到 RpcRequestMessageHandler
                }
            });
            //绑定端⼝,调⽤ sync ⽅法阻塞直到绑定完成
            Channel channel = serverBootstrap.bind(8080).sync().channel();
            //阻塞等待直到服务器Channel关闭(closeFuture()⽅法获取 Channel 的CloseFuture对象,然后调⽤sync()⽅法)
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("server error", e);
        } finally {
            //优雅关闭相关线程组资源
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
