package com.hualala.remoting.grpc;

import com.hualala.remoting.grpc.annotation.RpcService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RemotingTest.TestConfig.class)
public class RemotingTest {

    @Autowired
    @Qualifier("userServiceClient")
    UserService userService;

    @Test
    public void testInvocation() {
        long startTime  = System.currentTimeMillis();
        for(int  i=0 ;i<100000;i++){
            String username = userService.getUserById(1L);
            System.out.println(username);
            Assert.assertEquals("Expected username not  match", "Hello world", username);
        }
        long endTime  = System.currentTimeMillis();
        System.out.println("100000 Rpc call cost time"+(endTime - startTime)/1000);

    }

    @Configuration
    public static class TestConfig {

        @Bean
        public UserService userService() {
            return new UserServiceImpl();
        }

        @Bean
        public GrpcInvokerProxyFactoryBean grpcInvokerProxyFactoryBean() {
            GrpcInvokerProxyFactoryBean grpcInvokerProxyFactoryBean = new GrpcInvokerProxyFactoryBean();
            grpcInvokerProxyFactoryBean.setServiceUrl("localhost:8898");
            grpcInvokerProxyFactoryBean.setServiceInterface(UserService.class);
            return grpcInvokerProxyFactoryBean;
        }

        @Bean(name = "userServiceClient")
        public UserService userServiceClient() throws Exception {
            return (UserService) grpcInvokerProxyFactoryBean().getObject();
        }

        @Bean
        public GrpcInvokerServiceExporter userServiceServer() {
            GrpcInvokerServiceExporter grpcInvokerServiceExporter = new GrpcInvokerServiceExporter();
            grpcInvokerServiceExporter.setServiceInterface(UserService.class);
            grpcInvokerServiceExporter.setService(userService());
            grpcInvokerServiceExporter.setPort(8898);
            return grpcInvokerServiceExporter;
        }
    }

    public interface UserService {
        String getUserById(Long id);
    }

    @RpcService(value = UserService.class,version = "1.0.0")
    public static class UserServiceImpl implements UserService {

        @Override
        public String getUserById(Long id) {
            return "Hello world";
        }

    }
}
