package com.paw.engbridge.grpc;

import com.engbridge.auth.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.MetadataUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Component
public class UACClient {

    @Value("${uac.grpc.host}")
    private String host;

    @Value("${uac.grpc.port}")
    private int port;

    private ManagedChannel channel;
    private AuthServiceGrpc.AuthServiceBlockingStub authStub;

    @PostConstruct
    public void init() {
        this.channel = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .build();

        this.authStub = AuthServiceGrpc.newBlockingStub(channel);

        System.out.println("UAC gRPC Client initialized: " + host + ":" + port);
    }


    private AuthServiceGrpc.AuthServiceBlockingStub stubWithToken(String token) {
        Metadata headers = new Metadata();
        Metadata.Key<String> AUTH_KEY = Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);
        headers.put(AUTH_KEY, "Bearer " + token);

        return authStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(headers));
    }

    public LoginResponse login(LoginRequest request) {
        try {
            return authStub.login(request);
        } catch (StatusRuntimeException e) {
            throw new RuntimeException("Login failed", e);
        }
    }

    public RegisterResponse register(RegisterRequest request) {
        try {
            return authStub.register(request);
        } catch (StatusRuntimeException e) {
            throw new RuntimeException("Register failed", e);
        }
    }

    public ValidateResponse validateToken(String token) {
        try {
            ValidateRequest request = ValidateRequest.newBuilder()
                    .setToken(token)
                    .build();
            return authStub.validateToken(request);
        } catch (StatusRuntimeException e) {
            throw new RuntimeException("Token validation failed", e);
        }
    }

    public CreateUserResponse createUser(CreateUserRequest request, String token) {
        try {
            return stubWithToken(token).createUser(request);
        } catch (StatusRuntimeException e) {
            throw new RuntimeException("Create user failed", e);
        }
    }

    public UpdateResponse updateUser(UpdateRequest request, String token) {
        try {
            return stubWithToken(token).update(request);
        } catch (StatusRuntimeException e) {
            throw new RuntimeException("Update user failed", e);
        }
    }

    public DeleteResponse deleteUser(DeleteRequest request, String token) {
        try {
            return stubWithToken(token).delete(request);
        } catch (StatusRuntimeException e) {
            throw new RuntimeException("Delete user failed", e);
        }
    }

    public UserResponse getUser(UserIdRequest request, String token) {
        try {
            return stubWithToken(token).getUser(request);
        } catch (StatusRuntimeException e) {
            throw new RuntimeException("Get user failed", e);
        }
    }

    public UserList getAllUsers(String token) {
        try {
            Empty request = Empty.newBuilder().build();
            return stubWithToken(token).getAllUsers(request);
        } catch (StatusRuntimeException e) {
            throw new RuntimeException("Get all users failed", e);
        }
    }

    @PreDestroy
    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
            System.out.println("UAC gRPC Client shutdown");
        }
    }
}
