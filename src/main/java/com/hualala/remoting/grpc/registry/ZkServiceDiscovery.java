package com.hualala.remoting.grpc.registry;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Administrator on 2017/8/11.
 */
public class ZkServiceDiscovery {

    private static final Logger logger = LoggerFactory.getLogger(ZkServiceDiscovery.class);

    private static ZooKeeper zooKeeper;
    private static Lock reentrantLock = new ReentrantLock(true);
    private static Executor executor = Executors.newCachedThreadPool();

    private static ConcurrentMap<String, Set<String>> serviceAddress = new ConcurrentHashMap<String, Set<String>>();

    static {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    freshServiceAddress();
                    try {
                        TimeUnit.SECONDS.sleep(10L);
                    } catch (InterruptedException e) {
                        logger.error("", e);
                    }
                }
            }
        });
    }



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

                                // refresh service address
                                if (event.getType() == Event.EventType.NodeChildrenChanged || event.getState() == Event.KeeperState.SyncConnected) {
                                    freshServiceAddress();
                                }

                            }
                        });

                        logger.info("rpc zookeeper connnect success.");
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
            throw new NullPointerException("zookeeper connect fail.");
        }
        return zooKeeper;
    }





    public static void freshServiceAddress(){
        ConcurrentMap<String, Set<String>> resultMap = new ConcurrentHashMap<String, Set<String>>();
        try {
            List<String> interfaceNameList = getInstance().getChildren(Environment.ZK_SERVICES_PATH, true);
            if (!interfaceNameList.isEmpty()) {
                for (String interfaceName : interfaceNameList) {
                    String path = Environment.ZK_SERVICES_PATH.concat("/").concat(interfaceName);
                    List<String> addressList = getInstance().getChildren(path, true);
                    if (addressList != null && !addressList.isEmpty()) {
                        Set<String> addressSet = new HashSet<>();
                        for (String address : addressList) {
                            String addressPath = path.concat("/").concat(address);
                            byte[] bytes = getInstance().getData(addressPath, false, null);
                            addressSet.add(new String(bytes));
                        }
                        resultMap.put(interfaceName, addressSet);
                    }
                }
                serviceAddress = resultMap;
                logger.info(" rpc fresh serviceAddress success: {}", serviceAddress);
            }

        } catch (KeeperException e) {
            logger.error("", e);
        } catch (InterruptedException e) {
            logger.error("", e);
        }
    }
    public static String discover(String interfaceName) {
        logger.debug("discover:{}", serviceAddress);
        freshServiceAddress();
        Set<String> addressSet = serviceAddress.get(interfaceName);
        if (addressSet==null || addressSet.size()==0) {
            return null;
        }
        String address;
        List<String> addressArr = new ArrayList<String>(addressSet);
        int size = addressSet.toArray().length;
        if (size == 1) {
            address = addressArr.get(0);
        } else {
            address = addressArr.get(new Random().nextInt(size));
        }
        return address;
    }
}
