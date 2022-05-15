package com.chenlei.rpc.service;

import com.chenlei.rpc.annotation.server.RpcServer;

@RpcServer
public class HelloServiceImpl implements HelloService{
    @Override
    public String sayHello(String name) {

    return "你好, " + name;
    }
}
