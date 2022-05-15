package com.chenlei.rpc.registery;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.chenlei.rpc.loadBalancer.RoundRobinRule;
import com.chenlei.rpc.loadBalancer.LoadBalancer;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 服务发现接口
 *
 * @author chenlei
 */
public class NacosServerDiscovery implements ServerDiscovery {

    private final LoadBalancer loadBalancer;


    public NacosServerDiscovery(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer == null ? new RoundRobinRule() : loadBalancer;
    }


    /**
     * 根据服务名找到服务地址
     *
     * @param serviceName
     * @return
     */
    @Override
    public InetSocketAddress getService(String serviceName) throws NacosException {
        List<Instance> instanceList = NacosUtils.getAllInstance(serviceName);
        System.out.println(serviceName);
        if (instanceList.size() == 0) {
            throw new RuntimeException("找不到对应服务");
        }
        Instance instance = loadBalancer.getInstance(instanceList);
        return new InetSocketAddress(instance.getIp(), instance.getPort());
    }



}
