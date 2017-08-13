package com.hualala.remoting.grpc.serialize.impl;


import com.hualala.remoting.grpc.serialize.Serializer;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;

import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

/**
 * Created by Administrator on 2017/8/11.
 */
public class ProtostuffSerializer extends Serializer {


    @Override
    public <T> byte[] serialize(T obj) {
        Schema schema = RuntimeSchema.getSchema(obj.getClass());
        return ProtobufIOUtil.toByteArray(obj, schema, LinkedBuffer.allocate(256));
    }

    @Override
    public <T> Object deserialize(byte[] bytes, Class<T> clazz) {
        T obj = null;
        try {
            obj = clazz.newInstance();
            Schema schema = RuntimeSchema.getSchema(obj.getClass());
            ProtobufIOUtil.mergeFrom(bytes, obj, schema);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return obj;
    }
}
