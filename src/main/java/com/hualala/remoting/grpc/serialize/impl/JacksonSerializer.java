package com.hualala.remoting.grpc.serialize.impl;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hualala.remoting.grpc.serialize.Serializer;

import java.io.IOException;

/**
 * Created by Administrator on 2017/8/11.
 */
public class  JacksonSerializer extends Serializer {
    private final static ObjectMapper objectMapper = new ObjectMapper();

    /** bean、array、List、Map --> json
     * @param <T>*/
    @Override
    public <T> byte[] serialize(T obj) {
        try {
            return objectMapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /** string --> bean、Map、List(array) */
    @Override
    public <T> Object deserialize(byte[] bytes, Class<T> clazz)  {
        try {
            return objectMapper.readValue(bytes, clazz);
        } catch (JsonParseException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (JsonMappingException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

}
