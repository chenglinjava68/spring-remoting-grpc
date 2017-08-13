package com.hualala.remoting.grpc;


import com.google.protobuf.ByteString;
import com.hualala.remoting.grpc.serialize.Serializer;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.netty.util.internal.ThrowableUtil;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class RemotingServiceImpl extends RemotingServiceGrpc.RemotingServiceImplBase {

    private GrpcInvokerServiceExporter exporter;

    public RemotingServiceImpl(GrpcInvokerServiceExporter exporter) {
        this.exporter = exporter;
    }

    private Serializer serializer = Serializer.SerializeEnum.PROTOSTUFF.serializer;

    @Override
    public void execute(RemoteInvocationRequest request, StreamObserver<RemoteInvocationResponse> responseObserver) {
        try {
            //RemoteInvocation  remoteInvocation = (RemoteInvocation) serializer.deserialize(request.toByteArray(),RemoteInvocation.class);
            ByteArrayInputStream in = new ByteArrayInputStream(request.getData().toByteArray());
            ObjectInputStream is = new ObjectInputStream(in);
            RemoteInvocation remoteInvocation = (RemoteInvocation) is.readObject();
            RemoteInvocationResult remoteInvocationResult = exporter.invokeForInvocation(remoteInvocation);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(out);
            os.writeObject(remoteInvocationResult);
            responseObserver.onNext(RemoteInvocationResponse.newBuilder().setData(ByteString.copyFrom(out.toByteArray())).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            String stackTrace = ThrowableUtil.stackTraceToString(e);
            StatusRuntimeException statusException = Status.UNAVAILABLE.withDescription(stackTrace).asRuntimeException();
            responseObserver.onError(statusException);
        }
    }
    public void setSerializer(String serializer) {
        this.serializer = Serializer.SerializeEnum.match(serializer, Serializer.SerializeEnum.PROTOSTUFF).serializer;
    }
    public Serializer getSerializer() {
        return serializer;
    }
}
