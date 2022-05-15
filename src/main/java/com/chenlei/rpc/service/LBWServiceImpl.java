package com.chenlei.rpc.service;

import com.chenlei.rpc.annotation.server.RpcServer;

@RpcServer
public class LBWServiceImpl implements HelloService{
    @Override
    public String sayHello(String name) {
        return "卢本伟说你好"+name;
    }
}
