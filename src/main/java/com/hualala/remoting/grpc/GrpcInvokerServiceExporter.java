package com.hualala.remoting.grpc;

import com.hualala.remoting.grpc.annotation.RpcService;
import com.hualala.remoting.grpc.registry.ZkServiceRegistry;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.apache.zookeeper.KeeperException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.remoting.rmi.RemoteInvocationSerializingExporter;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;


public class GrpcInvokerServiceExporter extends RemoteInvocationSerializingExporter  implements ApplicationContextAware {

    private Server server;

    private int port;

    private static Map<String, Object> serviceMap = new HashMap<String, Object>();



    public RemoteInvocationResult invokeForInvocation(RemoteInvocation remoteInvocation) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return invokeAndCreateResult(remoteInvocation, getProxy());
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public void prepare() {
        super.prepare();
        try {
            ZkServiceRegistry.registerServices(port, serviceMap.keySet());
            start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void destroy() throws InterruptedException {
        blockUntilShutdown();
        ZkServiceRegistry.destory();
    }

    private void start() throws IOException {
        server = ServerBuilder.forPort(port).addService(new RemotingServiceImpl(this)).build().start();
        logger.info("Server started on port " + port);

        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                GrpcInvokerServiceExporter.this.stop();
            }
        });
    }

    private void stop() {
        if(server != null && !server.isShutdown()) {
            server.shutdown();

        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if(server != null) {
            server.awaitTermination();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(RpcService.class);
        if (serviceBeanMap!=  null && serviceBeanMap.size()>0) {
            for (Object serviceBean : serviceBeanMap.values()) {
                String interfaceName = serviceBean.getClass().getAnnotation(RpcService.class).value().getName();
                serviceMap.put(interfaceName, serviceBean);
            }
        }
    }
}
