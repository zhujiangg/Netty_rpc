package com.chenlei.rpc.nettyHandler;

import com.chenlei.rpc.message.Message;
import com.chenlei.rpc.message.PingMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * 心跳检测处理器
 *
 * @author chenlei
 */
@Slf4j
public class HeartBeatServerHandler extends ChannelDuplexHandler {
    /**
     * 处理特殊事件  IdleStateEvent
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            //读空闲事件
            if (event.state() == IdleState.READER_IDLE) {
                log.debug("长时间没有收到消息了，断开连接");
                ctx.close();
            }
        }
        super.userEventTriggered(ctx,evt);
    }

}