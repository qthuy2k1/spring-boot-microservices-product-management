package com.qthuy2k1.productservice.config;

import com.qthuy2k1.productservice.exception.CustomErrorDecoder;
import feign.codec.ErrorDecoder;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableMethodSecurity
@EnableWebSecurity
public class SecurityConfig {
    private final String[] PUBLIC_ENDPOINTS = {
            "/products/graphql", "/products/graphiql", "/actuator/prometheus", "/v3/api-docs/**", "/swagger-ui/**"
    };
    @Autowired
    JwtAuthConverter jwtAuthConverter;
    @Autowired
    JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain customSecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(request ->
                request.requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated());
        http.oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwtConfigurer -> jwtConfigurer
                                .jwtAuthenticationConverter(jwtAuthConverter))
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint));
        http.csrf(AbstractHttpConfigurer::disable);
        http.sessionManagement(t -> t.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    @Bean
    public DefaultMethodSecurityExpressionHandler msecurity() {
        DefaultMethodSecurityExpressionHandler defaultMethodSecurityExpressionHandler =
                new DefaultMethodSecurityExpressionHandler();
        defaultMethodSecurityExpressionHandler.setDefaultRolePrefix("");
        return defaultMethodSecurityExpressionHandler;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        // Already set prefix in service layer
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("roles");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info().title("My API").version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(
                        new io.swagger.v3.oas.models.Components()
                                .addSecuritySchemes(securitySchemeName,
                                        new SecurityScheme()
                                                .name(securitySchemeName)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT"))
                );
    }
}
