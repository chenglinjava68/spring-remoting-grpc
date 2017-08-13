package com.hualala.remoting.grpc.registry;

/**
 * Created by Administrator on 2017/8/11.
 */
public class Environment {
    /**
     * rpc service address on zookeeper, servicePath : /rpc/interfaceName/serverAddress(ip01:port9999)
     */
    public static final String ZK_SERVICES_PATH = "/rpc";

    /**
     * zk address
     */
    public static final String ZK_ADDRESS ="127.0.0.1:2181"	;

}
