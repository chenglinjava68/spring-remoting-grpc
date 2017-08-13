package com.hualala.remoting.grpc;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by Administrator on 2017/8/12.
 */
public class RpcResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private String requestId;
    private Throwable error;
    private Object data;
    private String code;
    private String message;


    public boolean isError() {
        return error != null;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public void getCode(String code) {
        this.code = code;
    }
    @Override
    public String toString() {
        return "requestId [requestId=" + requestId + ", error="
                + error + ", data =" + data
                + ", code=" + code + ", message="
                + message +  "]";
    }
}
