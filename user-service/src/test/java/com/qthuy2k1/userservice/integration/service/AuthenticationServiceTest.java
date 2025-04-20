package com.qthuy2k1.userservice.integration.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.qthuy2k1.userservice.dto.request.AuthenticationRequest;
import com.qthuy2k1.userservice.dto.response.AuthenticationResponse;
import com.qthuy2k1.userservice.dto.response.IntrospectResponse;
import com.qthuy2k1.userservice.enums.ErrorCode;
import com.qthuy2k1.userservice.exception.AppException;
import com.qthuy2k1.userservice.model.InvalidatedToken;
import com.qthuy2k1.userservice.repository.InvalidatedRepository;
import com.qthuy2k1.userservice.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest(properties = "spring.profiles.active=test")
@DirtiesContext
public class AuthenticationServiceTest extends BaseServiceTest {
    @Value("${spring.jwt.signerKey}")
    String SIGNER_KEY;
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private InvalidatedRepository invalidatedRepository;
    private String token;

    @BeforeEach
    void validAuthenticate() {
        AuthenticationRequest authenticationRequest = AuthenticationRequest.builder()
                .email(userSaved.getEmail())
                .password(USER_PASSWORD)
                .build();
        AuthenticationResponse authenticationResponse = authenticationService.authenticate(authenticationRequest);
        assertThat(authenticationResponse).isNotNull();
        assertThat(authenticationResponse.isAuthenticated()).isTrue();
        assertThat(authenticationResponse.getToken()).isNotBlank();
        token = "Bearer " + authenticationResponse.getToken(); //save the token.
    }

    @Test
    void authenticate_UserNotFound() {
        AuthenticationRequest authenticationRequest = AuthenticationRequest.builder()
                .email("notfound" + userSaved.getEmail())
                .password(USER_PASSWORD)
                .build();
        assertThatThrownBy(() ->
                authenticationService.authenticate(authenticationRequest))
                .isInstanceOf(AppException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    void authenticate_Unauthenticated() {
        AuthenticationRequest authenticationRequest = AuthenticationRequest.builder()
                .email(userSaved.getEmail())
                .password(USER_PASSWORD + "123")
                .build();
        assertThatThrownBy(() ->
                authenticationService.authenticate(authenticationRequest))
                .isInstanceOf(AppException.class)
                .hasMessageContaining(ErrorCode.UNAUTHENTICATED.getMessage());
    }

    @Test
    void introspect() throws ParseException, JOSEException {
        validAuthenticate();
        IntrospectResponse introspectResponse = authenticationService.introspect(token);

        assertThat(introspectResponse).isNotNull();
        assertThat(introspectResponse.isValid()).isTrue();
    }

    @Test
    void logout() throws ParseException, JOSEException {
        validAuthenticate();
        List<InvalidatedToken> invalidatedTokensBeforeLogout = invalidatedRepository.findAll();
        authenticationService.logout(token);

        List<InvalidatedToken> invalidatedTokensAfterLogout = invalidatedRepository.findAll();
        assertThat(invalidatedTokensAfterLogout.size() - invalidatedTokensBeforeLogout.size()).isEqualTo(1);

        IntrospectResponse introspectResponse = authenticationService.introspect(token);
        assertThat(introspectResponse).isNotNull();
        assertThat(introspectResponse.isValid()).isFalse();
    }

    @Test
    void refreshToken() throws ParseException, JOSEException {
        validAuthenticate();
        AuthenticationResponse refreshTokenAuthenticationResponse = authenticationService.refreshToken(token);

        assertThat(refreshTokenAuthenticationResponse).isNotNull();
        assertThat(refreshTokenAuthenticationResponse.isAuthenticated()).isTrue();
        assertThat(refreshTokenAuthenticationResponse.getToken()).isNotBlank();
    }


    private String generateExpiredToken() throws JOSEException {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject("test@example.com")
                .issuer("qthuy")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().minus(1, ChronoUnit.MINUTES).toEpochMilli())) // Expired 1 minute ago.
                .jwtID(UUID.randomUUID().toString())
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
        return jwsObject.serialize();
    }
}
