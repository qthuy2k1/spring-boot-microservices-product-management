package com.qthuy2k1.orderservice.config;

import com.qthuy2k1.orderservice.enums.ErrorCode;
import com.qthuy2k1.orderservice.exception.AppException;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
public class AuthenticationRequestInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        ServletRequestAttributes servletRequestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (servletRequestAttributes != null) {
            var authHeader = servletRequestAttributes.getRequest().getHeader("Authorization");

            if (StringUtils.hasText(authHeader)) {
                requestTemplate.header("Authorization", authHeader);
            }
        } else {
            log.info("Exception in AuthenticationRequestInterceptor: cannot get request header");
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

    }
}
