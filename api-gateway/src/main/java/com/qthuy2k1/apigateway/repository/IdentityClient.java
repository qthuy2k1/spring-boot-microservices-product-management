package com.qthuy2k1.apigateway.repository;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

import com.qthuy2k1.apigateway.dto.request.IntrospectRequest;
import com.qthuy2k1.apigateway.dto.response.ApiResponse;
import com.qthuy2k1.apigateway.dto.response.IntrospectResponse;

import reactor.core.publisher.Mono;

public interface IdentityClient {
    @PostExchange(url = "/auth/introspect", contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<ApiResponse<IntrospectResponse>> introspect(@RequestBody IntrospectRequest request);
}