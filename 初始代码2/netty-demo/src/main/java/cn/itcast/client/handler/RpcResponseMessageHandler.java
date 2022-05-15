package cn.itcast.client.handler;

import cn.itcast.message.RpcRequestMessage;
import cn.itcast.message.RpcResponseMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ChannelHandler.Sharable
public class RpcResponseMessageHandler extends SimpleChannelInboundHandler<RpcResponseMessage> {

    //要在很多次请求响应中记录promise对象，算有状态的，理应不能用@ChannelHandler.Sharable，但这里使用了 ConcurrentHashMap，考虑了线程安全，因此可以使用
    //                       序号   用来接收结果的 promise 对象
    public static final Map<Integer, Promise<Object>> PROMISES = new ConcurrentHashMap<>();

    /**
     * 7、最后，客户端的 handle 拿到消息后做一个打印
     * */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponseMessage msg) throws Exception {
        log.debug("{}", msg);
        // 根据拿到消息 的消息序号 得到空的 promise，且得到后要把它从 PROMISES 中移除，因此使用remove
        Promise<Object> promise = PROMISES.remove(msg.getSequenceId());
        if (promise != null) {
            Object returnValue = msg.getReturnValue();
            Exception exceptionValue = msg.getExceptionValue();
            //异常为null表示没有出错，返回正常结果，否则返回异常值
            /**
             *      setSuccess	-	设置成功结果
             *      setFailure	-	设置失败结果
             * */
            if(exceptionValue != null) {
                promise.setFailure(exceptionValue);
            } else {
                promise.setSuccess(returnValue);
            }
        }
    }
}
