package com.qthuy2k1.paymentservice.exception;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class CustomErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        String requestUrl = response.request().url();
        Response.Body responseBody = response.body();
        try {
            log.info("requestUrl: {}", requestUrl);
            return new ClientErrorException(response.status(), IOUtils.toString(responseBody.asReader(StandardCharsets.UTF_8)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
