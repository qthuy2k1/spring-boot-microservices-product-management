package com.qthuy2k1.apigateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qthuy2k1.apigateway.dto.response.ApiResponse;
import com.qthuy2k1.apigateway.enums.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class CustomAccessDeniedHandler implements ServerAccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        ApiResponse<Object> response = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        return writeResponse(exchange, response);
    }

    private Mono<Void> writeResponse(ServerWebExchange exchange, ApiResponse<Object> response) {
        ServerHttpResponse httpResponse = exchange.getResponse();
        httpResponse.setStatusCode(HttpStatus.FORBIDDEN);
        httpResponse.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(response);
            DataBuffer buffer = httpResponse.bufferFactory().wrap(bytes);
            return httpResponse.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            return httpResponse.setComplete();
        }
    }
}
