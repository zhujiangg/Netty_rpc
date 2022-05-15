package cn.itcast.server.handler;

import cn.itcast.message.RpcRequestMessage;
import cn.itcast.message.RpcResponseMessage;
import cn.itcast.server.service.HelloService;
import cn.itcast.server.service.ServicesFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
@ChannelHandler.Sharable
public class RpcRequestMessageHandler extends SimpleChannelInboundHandler<RpcRequestMessage> {

    /**
     * 4、拿到消息后，根据接口获取对象，方法。
     *   将异常或者反射获取的方法写入响应消息中
     * */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequestMessage message) {
        RpcResponseMessage response = new RpcResponseMessage();
        response.setSequenceId(message.getSequenceId());
        try {
            /**根据接口名找到接口实现类的对象
             *                     Class<?> interfaceClass = Class.forName(name);
             *                     Class<?> instanceClass = Class.forName(properties.getProperty(name));
             *                     map.put(interfaceClass, instanceClass.newInstance());
             * */
            HelloService service = (HelloService)
                                                // interfaceClass：获得接口实现类的Class
                    ServicesFactory.getService(Class.forName(message.getInterfaceName()));
            //通过反射调用对象的方法
            Method method = service.getClass().getMethod(message.getMethodName(), message.getParameterTypes());
            //调用对象的方法并输入参数返回结果：方法.invoke(对象，参数值)
            Object invoke = method.invoke(service, message.getParameterValue());
            response.setReturnValue(invoke);
        } catch (Exception e) {
            e.printStackTrace();
            /**
             *      客户端异常调用：
             *      response.setExceptionValue(e)，不需要将原始异常传回客户端（太长浪费资源），只需传一个错误原因
             * */
            String msg = e.getCause().getMessage();
            response.setExceptionValue(new Exception("远程调用出错:" + msg));
        }
        //无论成功与否，都需要将返回对象写回去
        ctx.writeAndFlush(response);
    }

    /**这里是反射获取对象方法实例，与本 RPC 无关*/
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        RpcRequestMessage message = new RpcRequestMessage(
                1,
                "cn.itcast.server.service.HelloService",
                "sayHello",
                String.class,
                new Class[]{String.class},
                new Object[]{"张三"}
        );

        HelloService service = (HelloService)
                ServicesFactory.getService(Class.forName(message.getInterfaceName()));
        Method method = service.getClass().getMethod(message.getMethodName(), message.getParameterTypes());
        Object invoke = method.invoke(service, message.getParameterValue());
        System.out.println(invoke);
    }
}
