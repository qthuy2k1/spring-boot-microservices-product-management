package com.qthuy2k1.apigateway.service;

import com.qthuy2k1.apigateway.dto.response.ApiResponse;
import com.qthuy2k1.apigateway.dto.response.IntrospectResponse;
import com.qthuy2k1.apigateway.repository.IdentityClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class IdentityService {
    IdentityClient identityClient;

    public Mono<ApiResponse<IntrospectResponse>> introspect(String token) {
        return identityClient.introspect(token);
    }
}
