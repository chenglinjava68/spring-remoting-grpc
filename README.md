目前想法是定义一个通用的proto 请求和响应 头
类似
syntax = "proto3";

package com.hualala.remoting.grpc;
option java_multiple_files = true;

message RemoteInvocationRequest {
     uint32 requestId =1;
     uint64 createMillisTime =2;
     string className  =3;
     string methodName =4;
     string parameterTypes =5;
     string parameters = 6;
}

message RemoteInvocationResponse {
     string requestId =1;
     string error =2;
     bytes  data =3;
     string code =4;
     string message = 5;
}

定义
 public interface UserService {
          String getUserById(Long id);
      }

  @RpcService(value = UserService.class,version = "1.0.0")
  public static class UserServiceImpl implements UserService{
  }
采用类似dubbo的方式 实现代理  目前 服务发现还没有想好 业务人员 无需关注proto 文件的定义 及proto 生成java

直接通过调用接口方式实现rpc 调用