package com.chenlei.rpc;

import com.chenlei.rpc.manager.ClientProxy;
import com.chenlei.rpc.manager.RpcClientManager;
import com.chenlei.rpc.service.HelloService;
import com.chenlei.rpc.service.LBWServiceImpl;

/**
 * 客户端测试
 *
 * @author chenlei
 */
public class RpcClient {
    public static void main(String[] args) {
            a();
    }

    private static void a() {
        RpcClientManager clientManager = new RpcClientManager();
        //创建代理对象
        HelloService service = new ClientProxy(clientManager).getProxyService(LBWServiceImpl.class);
        System.out.println(service.sayHello("zhangsan"));
    }
}
