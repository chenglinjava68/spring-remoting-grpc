package com.hualala.remoting.grpc.registry;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Administrator on 2017/8/11.
 */
public class ZkServiceRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ZkServiceRegistry.class);

    private static ZooKeeper zooKeeper;
    private static ReentrantLock reentrantLock = new ReentrantLock(true);

    private static ZooKeeper getInstance(){
        if (zooKeeper == null) {
            try {
                if (reentrantLock.tryLock(2, TimeUnit.SECONDS)) {

                    try {
                        zooKeeper = new ZooKeeper(Environment.ZK_ADDRESS, 30000, new Watcher() {
                            @Override
                            public void process(WatchedEvent event) {
                                // session expire, close old and create new
                                if (event.getState() == Event.KeeperState.Expired) {
                                    try {
                                        zooKeeper.close();
                                    } catch (InterruptedException e) {
                                        logger.error("", e);
                                    }
                                    zooKeeper = null;
                                }
                                // add One-time trigger, ZooKeeper的Watcher是一次性的，用过了需要再注册
                                try {
                                    String znodePath = event.getPath();
                                    if (znodePath != null) {
                                        zooKeeper.exists(znodePath, true);
                                    }
                                } catch (KeeperException e) {
                                    logger.error("", e);
                                } catch (InterruptedException e) {
                                    logger.error("", e);
                                }
                            }
                        });

                        logger.info(" rpc zookeeper connnect success.");
                    } finally {
                        reentrantLock.unlock();
                    }
                }
            } catch (InterruptedException e) {
                logger.error("", e);
            } catch (IOException e) {
                logger.error("", e);
            }
        }
        if (zooKeeper == null) {
            throw new NullPointerException("rpc, zookeeper connect fail.");
        }
        return zooKeeper;
    }

    public static void destory(){
        if (zooKeeper != null) {
            try {
                zooKeeper.close();
            } catch (InterruptedException e) {
                logger.error("", e);
            }
        }

    }
    public static void registerServices(int port, Set<String> serviceList) throws KeeperException, InterruptedException {
        // valid
        if (port < 1 || (serviceList==null || serviceList.isEmpty())) {
            return;
        }

        // init address: ip : port
        String ip = null;
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        }
        if (ip == null) {
            return;
        }
        String serverAddress = ip + ":" + port;

        // base path
        Stat stat = getInstance().exists(Environment.ZK_SERVICES_PATH, true);
        if (stat == null) {
            getInstance().create(Environment.ZK_SERVICES_PATH, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }

        // register
        for (String interfaceName : serviceList) {

            // init servicePath prefix : servicePath : xxl-rpc/interfaceName/serverAddress(ip01:port9999)
            String ifacePath = Environment.ZK_SERVICES_PATH.concat("/").concat(interfaceName);
            String addressPath = Environment.ZK_SERVICES_PATH.concat("/").concat(interfaceName).concat("/").concat(serverAddress);

            // ifacePath(parent) path must be PERSISTENT
            Stat ifacePathStat = getInstance().exists(ifacePath, true);
            if (ifacePathStat == null) {
                getInstance().create(ifacePath, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }

            // register service path must be EPHEMERAL
            Stat addreddStat = getInstance().exists(addressPath, true);
            if (addreddStat != null) {
                getInstance().delete(addressPath, -1);
            }
            String path = getInstance().create(addressPath, serverAddress.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            logger.info("rpc register service on zookeeper success, interfaceName:{}, serverAddress:{}", interfaceName, serverAddress);

        }

    }

}
