package com.hualala.remoting.grpc.annotation;

import java.lang.annotation.*;

/**
 * Created by Administrator on 2017/8/11.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RpcService {

    Class<?> value();

    String version() default "0.0";
}
