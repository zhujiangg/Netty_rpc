package com.chenlei.rpc.registery;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Nacos工具类管理连接获取等操作
 *
 * @author chenlei
 */
public class NacosUtils {

    private static final NamingService namingService;

    private static final Set<String> serviceNames = new HashSet<>();

    private static InetSocketAddress address;

    private static final String SERVER_ADDR = "127.0.0.1:8848";

    static {
        namingService = getNacosNamingService();
    }

    //初始化
    public static NamingService getNacosNamingService() {
        try {
            return NamingFactory.createNamingService(SERVER_ADDR);
        } catch (NacosException e) {
            throw new RuntimeException("连接到Nacos时发生错误");
        }
    }

    /**
     * 注册服务
     *
     * @param serverName
     * @param address
     * @throws NacosException
     */
    public static void registerServer(String serverName, InetSocketAddress address) throws NacosException {
        namingService.registerInstance(serverName, address.getHostName(), address.getPort());
        NacosUtils.address = address;
        serviceNames.add(serverName);
    }

    /**
     * 获取当前服务名中的所有实例
     *
     * @param serverName
     * @return
     * @throws NacosException
     */
    public static List<Instance> getAllInstance(String serverName) throws NacosException {
        return namingService.getAllInstances(serverName);
    }

    /**
     * 注销服务
     */
    public static void clearRegister() {
        if (!serviceNames.isEmpty() && address != null) {
            String host = address.getHostName();
            int port = address.getPort();
            Iterator<String> iterator = serviceNames.iterator();
            while (iterator.hasNext()) {
                String serviceName = iterator.next();
                try {
                    namingService.deregisterInstance(serviceName, host, port);
                } catch (NacosException e) {
                    new RuntimeException("注销服务失败");
                }
            }
        }

    }

}
