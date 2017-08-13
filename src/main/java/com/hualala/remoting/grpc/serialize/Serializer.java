package com.hualala.remoting.grpc.serialize;

import com.hualala.remoting.grpc.serialize.impl.HessianSerializer;
import com.hualala.remoting.grpc.serialize.impl.JacksonSerializer;
import com.hualala.remoting.grpc.serialize.impl.ProtostuffSerializer;

/**
 * Created by Administrator on 2017/8/11.
 */
public abstract class Serializer {


    public abstract <T> byte[] serialize(T obj);
    public abstract <T> Object deserialize(byte[] bytes, Class<T> clazz);

    public enum SerializeEnum {
        HESSIAN(new HessianSerializer()),
        PROTOSTUFF(new ProtostuffSerializer()),
        JSON(new JacksonSerializer());

        public final Serializer serializer;
        private SerializeEnum (Serializer serializer) {
            this.serializer = serializer;
        }
        public static SerializeEnum match(String name, SerializeEnum defaultSerializer){
            for (SerializeEnum item : SerializeEnum.values()) {
                if (item.name().equals(name)) {
                    return item;
                }
            }
            return defaultSerializer;
        }
    }
}
